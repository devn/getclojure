(ns getclojure.seed
  (:require
   [getclojure.extract :as extract]
   [getclojure.sexp :as sexp]
   [getclojure.elastic :as elastic]))

(defn seed
  ([num-logs]
   (seed num-logs {}))
  ([num-logs opts]
   (let [sexps (if (= num-logs :all)
                 (extract/all-sexps)
                 (extract/all-sexps num-logs))]

     (elastic/delete-and-recreate-index! elastic/conn)

     (elastic/seed elastic/conn
                   (->> sexps
                        sexp/filtered-run-coll
                        sexp/format-coll)
                   opts))))

(defn -main
  [& args]
  (if (seq args)
    (let [num-logs (-> args
                       first
                       read-string)]
      (seed num-logs))
    (println "Usage: lein seed-elastic-full OR lein seed-elastic-partial $NUMBER_OF_LOGFILES_TO_SEED"))

  (shutdown-agents)
  (System/exit 0))
