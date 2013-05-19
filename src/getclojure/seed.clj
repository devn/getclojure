(ns getclojure.seed
  (:require [clojure.java.io :as io]
            [monger.collection :as mc]
            [clojurewerkz.elastisch.rest :refer [connect! delete]]
            [clojurewerkz.elastisch.rest.index :refer [exists?]]
            [getclojure.db :refer [make-connection!]]
            [getclojure.models.sexp :refer [create-sexp!]]
            [getclojure.models.user :refer [create-user!]]
            [getclojure.search :refer [add-to-index
                                       create-getclojure-index]]
            [taoensso.timbre :refer [spy]]))

(def sexps
  (-> (io/file "resources/sexps/working-sexps.db")
      slurp
      read-string
      lazy-seq))

(defn seed-sexp [sexp-map]
  (let [user (create-user! "admin@getclojure.org" "admin")]
    (try
      (if-not (mc/any? "sexps" {:raw-input (:input sexp-map)})
        (let [id (:id (create-sexp! user sexp-map))]
          (add-to-index :getclojure (assoc sexp-map :id id))))
      (catch Exception _ (str "[ERROR] Could not seed: " sexp-map)))))

(defn seed-sexps [sexp-maps]
  (dorun (map seed-sexp sexp-maps)))

(defn clean-db! []
  (let [conn (make-connection!)
        env (:environment conn)]
    (if (= :development env)
      (do (mc/remove :users)
          (mc/remove :sexps)))))

(defn -main []
  (println "Attempting to connect to elastic search...")
  (let [search-endpoint (or (System/getenv "BONSAI_URL")
                            "http://127.0.0.1:9200")
        idx-name "getclojure"
        db-uri (str search-endpoint "/" idx-name)]
    (clean-db!)
    (connect! search-endpoint)
    (if (exists? idx-name) (delete db-uri))
    (create-getclojure-index)
    (spy (seed-sexps sexps))))

(comment
  (do
    (clean-db!)
    (connect! "http://127.0.0.1:9200")
    (when (exists? "getclojure") (delete search-endpoint))
    (create-getclojure-index)
    (spy (seed-sexps sexps)))
)
