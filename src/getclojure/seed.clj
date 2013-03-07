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
  (println "Attempting to connect to elastic search...")
  (let [search-endpoint (or (System/getenv "BONSAI_URL") "http://127.0.0.1:9200")]
    (println "The elastic search endpoint is" search-endpoint)

    (println "Connecting to" search-endpoint)
    (connect! search-endpoint)

    (let [idx-name "getclojure"]
      (if (exists? idx-name)
        (do (println "The index" idx-name "already existed!")
            (println "Deleting" idx-name "...")
            (delete idx-name))
        (println "The" idx-name "index doesn't exist..."))
      (println "Creating" idx-name "index...")
      (create-getclojure-index))
    (println "Populating the index...")
    (time (add-sexps-to-index))))