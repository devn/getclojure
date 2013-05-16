(ns getclojure.seed
  (:require [clojure.java.io :as io]
            [clojurewerkz.elastisch.rest :refer [connect!]]
            [clojurewerkz.elastisch.rest.index :refer [exists? create delete]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [getclojure.db :refer [make-connection! env?]]
            [getclojure.search :refer [create-getclojure-index add-to-index]]
            [getclojure.models.user :refer [create-user!]]
            [getclojure.models.sexp :refer [create-sexp!]]
            [taoensso.timbre :refer [spy]]))

(def sexps
  (-> (io/file "working-sexps.db")
      slurp
      read-string
      lazy-seq))

(defn seed-sexp [sexp-map]
  (let [user (create-user! "admin@getclojure.org" "admin")]
    (try
      (if-not (p :check-sexp-exists (mc/any? "sexps" {:raw-input (:input sexp-map)}))
        (let [id (:id (p :create-sexp! (create-sexp! user sexp-map)))]
          (p :add-to-index (add-to-index :getclojure (assoc sexp-map :id id)))))
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
        idx-name "getclojure"]
    (clean-db!)
    (connect! search-endpoint)
    (if (exists? idx-name) (delete idx-name))
    (create-getclojure-index)
    (spy (seed-sexps sexps))))

(comment
  (do
    (clean-db!)
    (connect! "http://127.0.0.1:9200")
    (if (exists? "getclojure") (delete "getclojure"))
    (create-getclojure-index)
    (spy (seed-sexps sexps)))
)
