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
            [taoensso.timbre :refer [spy info]]))

(def sexps
  (->> (io/file "working-sexps.db")
       slurp
       read-string
       (into #{})))

(defn seed-sexps [sexp-set]
  (let [numbered-sexps (sort-by key (zipmap (iterate inc 1) sexp-set))
        cnt (count numbered-sexps)
        user (create-user! "admin@getclojure.org" "admin")]
    (doseq [[n sexp] numbered-sexps]
      (info (str n "/" cnt))
      (try
        (if-not (mc/any? "sexps" {:raw-input (:input sexp)})
          (let [id (:id (create-sexp! user sexp))]
            (add-to-index :getclojure (assoc sexp :id id))))
        (catch Exception _ (str sexp ": failed to be seeded!"))))))

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
    (spy (time (seed-sexps sexps)))))