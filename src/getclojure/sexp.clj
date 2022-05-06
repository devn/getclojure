(ns getclojure.sexp
  (:refer-clojure :exclude [eval])
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [getclojure.util :as util]
   [libpython-clj2.require :refer [require-python]]
   [outpace.config :refer [defconfig]]
   [sci.core :as sci]
   [taoensso.timbre :as log])
  (:import
   (com.algolia.search SearchClient SearchIndex DefaultSearchClient)
   (com.algolia.search.models.indexing SearchResult Query)
   (java.io StringWriter)
   (java.util.concurrent TimeUnit FutureTask TimeoutException)))


                                        ; SEARCH

(defconfig algolia-app-id)
(defconfig algolia-admin-api-key)
(defconfig algolia-index)

(def ^:private search-client
  (delay (DefaultSearchClient/create algolia-app-id algolia-admin-api-key)))

(def ^:private search-index
  (delay (.initIndex ^SearchClient @search-client "getclojure_production")))

(defn search
  ([q] (search q 0))
  ([q page-num]
   (let [res (.search ^SearchIndex @search-index (.. (Query. q)
                                                     (setAttributesToRetrieve ["formatted-input"
                                                                               "formatted-output"
                                                                               "formatted-value"])
                                                     (setHitsPerPage (int 25))
                                                     (setPage (int page-num))))]
     {:hits       (.getHits ^SearchResult res)
      :total-hits (.getNbHits ^SearchResult res)
      :pages      (.getNbPages ^SearchResult res)})))

                                        ; FORMAT

(require-python 'pygments)
(require-python 'pygments.lexers)
(require-python 'pygments.formatters)

(defn ^:private pygmentize
  [s]
  (pygments/highlight s
                      (pygments.lexers/get_lexer_by_name "Clojure")
                      (pygments.formatters/get_formatter_by_name "html")))

(defn ^:private format-input
  [s]
  (binding [*read-eval* false]
    (pygmentize (with-out-str s
                  (pp/with-pprint-dispatch pp/code-dispatch
                    (pp/pprint (read-string s)))))))

(defn ^:private format-value
  [s]
  (pygmentize s))

(defn ^:private format-output
  [s]
  (binding [*read-eval* false]
    (when-not (= s "\"\"")
      (pygmentize
       (read-string
        (with-out-str (pp/with-pprint-dispatch pp/code-dispatch
                        (pp/pprint s))))))))

(defn format-coll
  [sexp-maps]
  (mapv (fn [{:keys [input value output] :as m}]
          (try (merge m {:formatted-input (format-input input)
                         :formatted-value (format-value value)
                         :formatted-output (format-output output)})
               (catch Exception _e
                 (log/warn {:input input
                            :value value
                            :output output}))))
        sexp-maps))

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
         (catch Exception e
           (log/warn e)
           (.cancel task true)
           (.stop thread)))))

(defn ^:private run-coll
  [timeout-millis sexp-coll]
  (reduce (fn [res m]
            (if-let [result (thunk-timeout (fn [] (run (:input m)))
                                           timeout-millis)]
              (conj res result)
              res))
          []
          sexp-coll))

(defn ^:private remove-junk [coll]
  (remove (fn [{:keys [input]}]
            (or (str/starts-with? input "(doc")
                (str/starts-with? input "(source")
                ;; NOTE: There's a problem with pprint code-dispatch when
                ;; printing fn*. See:
                ;; http://dev.clojure.org/jira/browse/CLJ-1181
                (str/includes? input "fn*")))
          coll))

(defn build-formatted-collection
  [filename]
  (->> (io/resource filename)
       slurp
       read-string
       remove-junk
       (into #{})
       (run-coll 5000)
       (format-coll)))

(defn generate-algolia-json-file
  [filename]
  (spit "output.json"
        (->> (build-formatted-collection filename)
             (json/encode))))

(defn -main [& args]
  (log/info "Generating algolia \"output.json\" file for directly loading into Algolia.")
  (time (generate-algolia-json-file "sexps/working-sexps.db"))
  (shutdown-agents)
  (System/exit 0))
