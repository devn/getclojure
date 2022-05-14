(ns getclojure.seed
  (:require
   [getclojure.extract :as extract]
   [getclojure.sexp :as sexp]
   [getclojure.elastic :as elastic]
   [taoensso.timbre :as log]))

(defn -main
  [& args]
  (if (seq args)
    (let [num-logs (read-string (first args))
          sexps (if (= num-logs :all)
                  (extract/all-sexps)
                  (extract/all-sexps num-logs))]

      (elastic/delete-and-recreate-index! elastic/conn)

      (->> sexps
           sexp/filtered-run-coll
           sexp/format-coll
           (elastic/seed elastic/conn)))
    (println "Usage: lein seed-elastic-full OR lein seed-elastic-partial $NUMBER_OF_LOGFILES_TO_SEED"))

  (shutdown-agents)
  (System/exit 0))
