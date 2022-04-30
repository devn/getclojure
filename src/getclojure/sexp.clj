(ns getclojure.sexp
  (:refer-clojure :exclude [eval])
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [getclojure.util :as util]
   [outpace.config :refer [defconfig]]
   [sci.core :as sci])
  (:import
   (java.io StringWriter)
   (java.util.concurrent TimeUnit FutureTask TimeoutException)
   (com.algolia.search SearchClient SearchIndex DefaultSearchClient)
   (com.algolia.search.models.indexing SearchResult Query)))

                                        ; SEARCH
(defconfig algolia-app-id)
(defconfig algolia-admin-api-key)
(defconfig algolia-index)

(def search-client (delay (DefaultSearchClient/create algolia-app-id algolia-admin-api-key)))

(def search-index (delay (.initIndex ^SearchClient @search-client "getclojure_production")))

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

(defn ^:private pygmentize
  [s]
  (:out (sh/sh "pygmentize" "-fhtml" "-lclojure" :in s)))

(defn ^:private format-input
  [s]
  (pygmentize (with-out-str s
                (pp/with-pprint-dispatch pp/code-dispatch
                  (pp/pprint (read-string s))))))

(defn ^:private format-value
  [s]
  (pygmentize s))

(defn ^:private format-output
  [s]
  (when-not (= s "\"\"")
    (pygmentize
     (read-string
      (with-out-str (pp/with-pprint-dispatch pp/code-dispatch
                      (pp/pprint s)))))))

(defn format-coll
  [sexp-maps]
  (doall
   (pmap (fn [{:keys [input value output] :as m}]
           (try (let [input-fmt (future (format-input input))
                      value-fmt (future (format-value value))
                      output-fmt (future (format-output output))]
                  (merge m {:formatted-input @input-fmt
                            :formatted-value @value-fmt
                            :formatted-output @output-fmt}))
                (catch Exception _e
                  (println input value output))))
         sexp-maps)))

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

(defn run-coll
  [timeout-millis sexp-coll]
  (reduce (fn [res m]
            (if-let [result (thunk-timeout (fn [] (run (:input m)))
                                           timeout-millis)]
              (conj res result)
              res))
          []
          sexp-coll))

(defn -main []
  ;; Generate json of all sexps for use in Algolia
  (spit "output.json"
        (->> (io/resource "sexps/working-sexps.db")
             slurp
             read-string
             (remove (fn [{:keys [input]}]
                       (or (str/starts-with? input "(doc")
                           (str/starts-with? input "(source")
                           ;; NOTE: There's a problem with pprint code-dispatch when
                           ;; printing fn*. See:
                           ;; http://dev.clojure.org/jira/browse/CLJ-1181
                           (str/includes? input "fn*"))))
             (into #{})
             (run-coll 5000)
             (format-coll)
             (json/encode)))
  (shutdown-agents)
  (System/exit 0))
