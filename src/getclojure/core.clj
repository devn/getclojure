(ns getclojure.core
  (:use [getclojure.jail :only (run-sexp-in-sandbox)]
        [getclojure.scrape :only (local-logs get-missing-logs)]
        [getclojure.extract :only (log->mapseq)]
        [taoensso.timbre :as timbre :only (trace debug info warn error fatal spy)])
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query :as q])
  (:import java.util.concurrent.TimeoutException))

(def connect-elastisch (esr/connect! "http://127.0.0.1:9200"))

(def mappings
  {:sexp
   {:properties
    {:input {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:output {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:value {:type "string" :store "yes" :analyzer "clojure_code"}}}})

(def clojure-analyzer
  {:clojure_code {:type "standard"
                  :filter ["standard" "lowercase" "stop"]
                  :stopwords ["(" ")" "[" "]" "{" "}" "#" "%"]}})

(defn create-getclojure-index []
  (when-not (esi/exists? "getclojure_development")
    (esi/create "getclojure_development"
                :settings {:index {:analysis {:analyzer clojure-analyzer}}}
                :mappings mappings)))

(create-getclojure-index)

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(def sexps (atom []))

(defn add-sexp-to-sexps-atom [m]
  (swap! sexps conj m))

(defn process-log [log]
  (println "Extracting nodes from" (str log))
  (doseq [sexp-node (filter #(not (empty? (:sexp %))) (log->mapseq log))]
    (doseq [sexp (:sexp sexp-node)]
      (let [id (uuid)]
        (try
          (let [sexp-output-map (run-sexp-in-sandbox sexp)]
            (spy :info (time (add-sexp-to-sexps-atom (assoc sexp-output-map :id (uuid)))))
            (spy :info (time (esd/put "getclojure_development" "sexp" id sexp-output-map))))
          (catch TimeoutException _ "Execution timed out!")
          (catch Throwable t))))))

(defn process-all-logs [fcoll]
  (doseq [log fcoll]
    (process-log log)))

(comment
  (doseq [log (take 1000 local-logs)]
    (process-log log))

  (process-log (nth local-logs 4))
  (process-all-logs local-logs)

  @sexps
  (println (count @sexps))

  (esi/refresh "getclojure_development")
  (esd/search "getclojure_development" "sexp" :query (q/text :input ""))
  (esd/search-all-indexes-and-types "getclojure_development"
                                    :query (q/text :input "import"))

  (create-getclojure-index)
  (esi/delete "getclojure_development")
)
