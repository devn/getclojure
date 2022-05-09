(ns getclojure.sexp
  (:refer-clojure :exclude [eval])
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [getclojure.format :as fmt]
   [getclojure.util :as util]
   [schema.core :as s]
   [sci.core :as sci]
   [taoensso.timbre :as log])
  (:import
   (java.io StringWriter)
   (java.util.concurrent TimeUnit FutureTask TimeoutException)))

(s/set-fn-validation! true)

                                        ; EVALUATE

(defn ^:private eval
  "Evaluate a string in SCI. Defined separately in case we want to supply
  additional options to sci/eval-string in the future."
  [sexp-str]
  (sci/eval-string sexp-str))

(defn ^:private run
  "Runs a string like \"(inc 1)\" in SCI, and returns a map containing its :input,
  :output, and :value."
  [sexp-str]
  (with-open [w (new StringWriter)]
    (sci/binding [sci/out w]
      {:input sexp-str
       :value (util/truncate 400 (pr-str (eval sexp-str)))
       :output (util/truncate 400 (pr-str (str w)))})))

(defn ^:private thunk-timeout
  "Cancelling a future is insufficient. See amalloy's answer on StackOverflow
  here: https://stackoverflow.com/a/6697356"
  [thunk millis]
  (let [task (FutureTask. thunk)
        thread (Thread. task)]
    (try (.start thread)
         (.get task millis TimeUnit/MILLISECONDS)
         (catch TimeoutException _timeout-e
           (.cancel task true)
           (.stop thread))
         (catch Exception _e
           (.cancel task true)
           (.stop thread)))))

(defn ^:private run-coll
  [timeout-millis sexp-coll]
  (persistent!
   (reduce (fn [res s]
             (if-let [result (thunk-timeout (fn [] (run s))
                                            timeout-millis)]
               (conj! res result)
               res))
           (transient [])
           sexp-coll)))

(defn ^:private remove-junk
  [coll]
  (remove (fn [input]
            (or (str/starts-with? input "(doc")
                (str/starts-with? input "(source")
                ;; NOTE: There's a problem with pprint code-dispatch when
                ;; printing fn*. See:
                ;; http://dev.clojure.org/jira/browse/CLJ-1181
                (str/includes? input "fn*")))
          coll))

(defn working-sexps
  [filename]
  (->> (io/resource filename)
       slurp
       read-string
       remove-junk
       (run-coll 250)))

(defn generate-formatted-collection
  [filename]
  (log/info "Generating formatted-sexps.edn")
  (time (spit "resources/sexps/formatted-sexps.edn"
              (->> (io/resource filename)
                   slurp
                   read-string
                   format-coll))))

(defn generate-algolia-json-file
  [filename]
  (log/info "Generating algolia output.json")
  (time (spit "output.json"
              (->> (io/resource filename)
                   slurp
                   read-string
                   format-coll
                   (json/encode)))))

(defn generate-working-sexps-file
  [filename]
  (log/info "Generating working-sexps.edn")
  (time (spit "resources/sexps/working-sexps.edn"
              (working-sexps filename))))

(defn -main [& args]
  (let [op (first args)]
    (case op
      "working" (generate-working-sexps-file "sexps/input.edn")
      "algolia" (generate-algolia-json-file "sexps/working-sexps.edn")
      "formatted" (generate-formatted-collection "sexps/working-sexps.edn")
      (do (println "Valid arguments: working, algolia, or formatted.")
          (println " - `working` produces `sexps/working-sexps.edn` from `sexps/input.edn` which contains all s-expressions which run in SCI")
          (println " - `algolia` produces `output.json` from `sexps/working-sexps.edn`for consumption by Algolia")
          (println " - `formatted` produces `sexps/formatted-sexps.edn` from `sexps/working-sexps.edn`for ElasticSearch"))))
  (shutdown-agents)
  (System/exit 0))
