(ns getclojure.models.sexp
  (:refer-clojure :exclude [sort find])
  (:require [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :refer [with-collection find sort limit paginate]]
            [getclojure.format :refer [format-input format-output format-value]]))

(let [uri (or (System/getenv "MONGODB_URI")
              "mongodb://127.0.0.1/getclojure_development")
      uri "mongodb://heroku_app11300183:5i1uhb3oojqo6da8qe829f58c0@ds029297.mongolab.com:29297/heroku_app11300183"]
  (println "Connecting to Mongo URI:" uri)
  (mg/connect-via-uri! uri))

(def sexp-id
  "The current highest sexp-id."
  (atom
   (-> (with-collection "sexps"
         (find {})
         (sort {:id -1})
         (limit 1))
       first
       :id
       (or 0))))

(defn sexp-exists? [{:keys [raw-input]}]
  (mc/any? "sexps" {:raw-input raw-input}))

(defn sexp-record-map [id user sexp-map]
  (let [{:keys [input value output]} sexp-map]
    {:id id
     :user (:_id user)
     :raw-input input
     :raw-value value
     :raw-output output
     :input (format-input input)
     :value (format-value value)
     :output (format-output output)}))

(defn create-sexp!
  "Create a new sexp"
  [user sexp-map]
  (let [id (swap! sexp-id inc)
        sexp (sexp-record-map id user sexp-map)]
    (if-not (sexp-exists? sexp)
      (mc/insert-and-return "sexps" sexp))))