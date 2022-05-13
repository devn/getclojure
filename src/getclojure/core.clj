(ns getclojure.core
  (:require
   [getclojure.extract :as extract]
   [getclojure.sexp :as sexp]
   [getclojure.elastic :as elastic]))

(defn -main
  [& args]
  (elastic/delete-and-recreate-index! elastic/conn)
  (->> (extract/all-sexps)
       sexp/filtered-run-coll
       sexp/format-coll
       (elastic/seed-sexp-coll elastic/conn))
  (shutdown-agents)
  (System/exit 0))
