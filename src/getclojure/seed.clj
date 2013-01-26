(ns getclojure.seed
  (:use [getclojure.search :only (create-getclojure-index add-to-index)]
        [clojurewerkz.elastisch.rest :only (connect!)]
        [clojurewerkz.elastisch.rest.index :only (exists? create delete)])
  (:require [clojure.java.io :as io]))

(def sexps
  (into #{} (read-string (slurp (io/file "working-sexps.db")))))

(defn add-sexps-to-index []
  (let [num-sexps (count sexps)
        cnt (atom 0)]
    (doseq [sexp sexps]
      (swap! cnt inc)
      (println (str @cnt "/" num-sexps))
      (add-to-index :getclojure sexp))))

(defn -main []
  (println "Attempting to connect to searchbox...")
  (println "The Bonsai URL is" (System/getenv "BONSAI_URL"))
  (connect! (or (System/getenv "BONSAI_URL")
                "http://127.0.0.1:9200"))
  (doseq [idx ["getclojure" "getclojure_development"]]
    (if (exists? idx)
      (println "Deleting" idx "...")
      (delete idx)))
  (println "Creating getclojure index...")
  (create-getclojure-index)
  (println "Adding sexps to the index...")
  (time (add-sexps-to-index)))