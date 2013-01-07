(ns getclojure.core
  (:use [getclojure.jail :only (run-sexp-in-sandbox)]
        [getclojure.scrape :only (local-logs missing-logs get-missing-log get-missing-logs)]
        [getclojure.extract :only (log->mapseq)]
        [getclojure.queue])
  (:require [clojure.java.io :as io])
  (:import [java.util.concurrent.TimeoutException]))

(def sexps (atom #{}))

(defn extract-sexps-from-log [log]
  (println "Extracting nodes from" (str log))
  (doseq [sexp-node (filter #(not (empty? (:sexp %))) (log->mapseq log))]
    (doseq [sexp (:sexp sexp-node)]
      (swap! sexps conj sexp)))
  nil)

(defn extract-sexps-from-all-logs [fcoll]
  (doseq [logfile fcoll]
    (extract-sexps-from-log logfile)))

(defn process-log [log]
  (println "Extracting nodes from" (str log))
  (doseq [sexp-node (filter #(not (empty? (:sexp %))) (log->mapseq log))]
    (doseq [sexp (:sexp sexp-node)]
      (try
        (run-sexp-in-sandbox sexp)
           (catch java.util.concurrent.TimeoutException _ "Execution timed out!")
           (catch Throwable t)))))

(defn process-all-logs [fcoll]
  (doseq [log fcoll]
    (process-log log)))

(comment
  (defn -main []
    (doseq [f local-logs]
      (println f)
      (process-log f)))
)
