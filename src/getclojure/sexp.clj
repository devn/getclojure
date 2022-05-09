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

(def default-truncation-length 400)
(def default-timeout-millis 250)

(s/defn ^:private eval :- s/Any
  "Evaluate a string in SCI. Defined separately in case we want to supply
  additional options to sci/eval-string in the future."
  [sexp-str :- s/Str]
  (sci/eval-string sexp-str))

(s/defschema SexpResult
  {:input s/Str
   :output s/Str
   :value s/Str})

(s/defn ^:private run :- SexpResult
  "Runs a string like \"(inc 1)\" in SCI, and returns a map containing its :input,
  :output, and :value."
  [sexp-str :- s/Str
   truncation-length :- s/Num]
  (with-open [w (new StringWriter)]
    (sci/binding [sci/out w]
      {:input sexp-str
       :value (util/truncate truncation-length (pr-str (eval sexp-str)))
       :output (util/truncate truncation-length (pr-str (str w)))})))

(s/defn ^:private thunk-timeout :- s/Any
  "Provided a `thunk` (fn [] ...) where ... is any expression, and `millis` is
  the number of milliseconds we'll wait before cancelling our task and the
  thread it runs in, returns the result of the expression inside the thunk
  unless it throws an Exception or does not complete within `millis`.

  Cancelling a future is insufficient. See amalloy's answer on StackOverflow
  here: https://stackoverflow.com/a/6697356"
  [thunk
   millis :- s/Num]
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

(s/defschema SexpColl
  (s/conditional sequential? [s/Str] set? #{s/Str}))

(s/defn ^:private run-coll :- [SexpResult]
  "Provided `timeout-millis` and a collection of s-expressions, runs each
  expression in SCI, returning only the ones that successfully run."
  [truncation-length :- s/Num
   timeout-millis :- s/Num
   sexp-coll :- SexpColl]
  (persistent!
   (reduce (fn [res s]
             (if-let [result (thunk-timeout (fn [] (run s truncation-length))
                                            timeout-millis)]
               (conj! res result)
               res))
           (transient [])
           sexp-coll)))

(s/defn ^:private remove-junk :- SexpColl
  "Provided a collection of strings, removes strings which are not interesting
  examples of expressions to search for."
  [coll :- SexpColl]
  (remove (fn [input]
            (or (str/starts-with? input "(doc")
                (str/starts-with? input "(source")
                ;; NOTE: There's a problem with pprint code-dispatch when
                ;; printing fn*. See:
                ;; http://dev.clojure.org/jira/browse/CLJ-1181
                (str/includes? input "fn*")))
          coll))

(s/defschema FormattedSexpResult
  (merge SexpResult
         {:formatted-input s/Str
          :formatted-value s/Str
          :formatted-output (s/maybe s/Str)}))

(s/defn format-coll :- [FormattedSexpResult]
  "Provided a collection of s-expression maps, returns a map containing the
  formatted versions alongside the original input, output, and value. Logs a
  warning for expressions which fail formatting."
  [sexp-maps :- [SexpResult]]
  (mapv (fn [{:keys [input value output] :as m}]
          (try (merge m {:formatted-input (fmt/input input)
                         :formatted-value (fmt/value value)
                         :formatted-output (fmt/output output)})
               (catch Throwable _t
                 (log/warn {:input input
                            :value value
                            :output output}))))
        sexp-maps))

(s/defn read-resource :- s/Any
  [filename :- s/Str]
  (->> (io/resource filename)
       slurp
       read-string))

(s/defn working-sexps :- [SexpResult]
  "Provided a `filename`, reads a file containing a collection of s-expressions
  as strings. Runs each s-expression in SCI. Returns a collection of evaluated
  s-expressions."
  [filename :- s/Str]
  (->> (read-resource filename)
       remove-junk
       (run-coll default-truncation-length default-timeout-millis)))

(s/defn generate-formatted-collection
  "Provided a `filename`, reads a file containing a collection of maps of the
  form {:input ... :output ... :value ...} and writes to a file the formatted
  collection of the form: [{:input ... :value ... :output ... :formatted-input
  ... :formatted-value ... :formatted-output ...}]"
  [filename :- s/Str]
  (log/info "Generating formatted-sexps.edn")
  (->> filename
       read-resource
       format-coll
       (spit "resources/sexps/formatted-sexps.edn")))

(s/defn generate-algolia-json-file
  "Provided a `filename`, reads a file containing a collection of maps of the
  form {:input ... :output ... :value ...} and writes to a file a json-encoded
  collection of the form: [{\"input\" ... \"value\" ... \"output\" ...
  \"formatted-input\" ... \"formatted-value\" ... \"formatted-output\" ...}] for
  uploading to an algolia index."
  [filename :- s/Str]
  (log/info "Generating algolia output.json")
  (spit "output.json"
        (->> (read-resource filename)
             format-coll
             (json/encode))))

(s/defn generate-working-sexps-file
  [filename :- s/Str]
  (log/info "Generating working-sexps.edn")
  (spit "resources/sexps/working-sexps.edn" (working-sexps filename)))

(defn -main [& args]
  (let [op (first args)]
    (case op
      "working" (generate-working-sexps-file "sexps/input.edn")
      "algolia" (generate-algolia-json-file "sexps/working-sexps.edn")
      "formatted" (generate-formatted-collection "sexps/working-sexps.edn")
      (do (println "Valid arguments: working, algolia, or formatted.")
          (println " - `working` produces `sexps/working-sexps.edn` from `sexps/input.edn` which contains all s-expressions which run in SCI")
          (println " - `algolia` produces `output.json` from `sexps/working-sexps.edn` for consumption by Algolia")
          (println " - `formatted` produces `sexps/formatted-sexps.edn` from `sexps/working-sexps.edn` for ElasticSearch"))))

  (shutdown-agents)
  (System/exit 0))

(comment
  (require '[criterium.core :as criterium])

  (criterium/quick-bench
   (inc 1))

  (require '[clj-async-profiler.core :as prof])
  (prof/profile {:width 2500 :min-width 3}
                (working-sexps "sexps/input.edn"))

  (def server (prof/serve-files 8081))

  )
