(ns getclojure.core
  (:require
   [getclojure.extract :as extract]
   [getclojure.sexp :as sexp]
   [getclojure.elastic :as elastic]))

(defn -main
  [& args]
  (let [num-logs (read-string (first args))
        sexps (if (= num-logs :all)
                (extract/all-sexps)
                (extract/all-sexps num-logs))]

    (elastic/delete-and-recreate-index! elastic/conn)

    (->> sexps
         sexp/filtered-run-coll
         sexp/format-coll
         (elastic/seed elastic/conn))

    (shutdown-agents)
    (System/exit 0)))
