(ns getclojure.core
  (:use [getclojure.jail :only (run-sexp-in-sandbox)]
        [getclojure.scrape :only (local-logs missing-logs get-missing-log get-missing-logs clojuredocs-sexp-harvest get-sexps-from-clojuredocs)]
        [getclojure.extract :only (logfile->mapseq)]
        [getclojure.search :only (create-getclojure-index add-to-index)]
        [getclojure.repl :only (start-server)]
        [getclojure.handler :only (init)])
  (:require [clojure.java.io :as io]
            [clojurewerkz.elastisch.rest :as esr])
  (:import [java.util.concurrent.TimeoutException]))

(comment
  (do (set! *print-length* 10)
      (set! *print-level* 10))
)

(def sexps (into #{} (read-string (slurp (io/file "working-sexps.db")))))

(defn add-sexps-to-index []
  (doseq [sexp sexps]
    (add-to-index :getclojure sexp))
  (println "Work complete!"))

;; (add-sexps-to-index)

;; (def sexps (atom #{}))

;; (defn extract-sexps-from-log [log]
;;   (println "Extracting nodes from" (str log))
;;   (doseq [sexp-node (filter #(not (empty? (:sexp %))) (log->mapseq log))]
;;     (doseq [sexp (:sexp sexp-node)]
;;       (swap! sexps conj sexp)))
;;   nil)

;; (defn extract-sexps-from-all-logs [fcoll]
;;   (doseq [logfile fcoll]
;;     (extract-sexps-from-log logfile)))

;; (defn process-log [log]
;;   (println "Extracting nodes from" (str log))
;;   (doseq [sexp-node (filter #(not (empty? (:sexp %))) (log->mapseq log))]
;;     (doseq [sexp (:sexp sexp-node)]
;;       (try
;;         (run-sexp-in-sandbox sexp)
;;            (catch java.util.concurrent.TimeoutException _ "Execution timed out!")
;;            (catch Throwable t)))))

;; (defn process-all-logs [fcoll]
;;   (doseq [log fcoll]
;;     (process-log log)))

;; (def sexps (atom (read-string (slurp "sexps.db"))))

;; (def sandboxed-sexps-results (atom (read-string (slurp "sandboxed-sexps-results.db"))))

;; (def working-sexps (atom []))
;; (def n-of-sexps (atom 0))
;; (def total-sexps (count @clojuredocs-sexp-harvest))

;; (defn process-sexps [ss]
;;   (doseq [s ss]
;;     (swap! n-of-sexps inc)
;;     (println (str @n-of-sexps "/" total-sexps))
;;     (try (swap! working-sexps conj (run-sexp-in-sandbox s))
;;          (catch java.util.concurrent.TimeoutException _ "Execution timed out!")
;;          (catch java.lang.Throwable t))))

;; 4203 stalls.

;; (def working-sexps (atom (read-string (slurp "working-sexps.db"))))

;; (defn write-working-sexps-to-csv [wsexps]
;;   (with-open [o (io/writer "working-sexps-with-clojuredocs.csv")]
;;     (csv/write-csv o (into [] (map #(into [] (vals %)) wsexps)))))

;; (write-working-sexps-to-csv (set @sandboxed-sexps-results))

;; (binding [*print-dup* true]
;;   (spit (io/file "working-sexps-with-clojuredocs.db")
;;         (set @sandboxed-sexps-results)))

;; (defn -main []
;;   (process-sexps @sexps)
;;   (binding [*print-dup* true]
;;     (spit (io/file "working-sexps.db") @working-sexps)))

(comment
  (count (filter #(.contains (:input %) "(let") sexps))
)