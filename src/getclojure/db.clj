(ns getclojure.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(def mongo-uri
  (let [uri (get (System/getenv)
                 "MONGOLAB_URI"
                 "mongodb://127.0.0.1/getclojure_development")]
    (println "Mongo URI:" mongo-uri)))

(defn env? []
  (if (.contains mongo-uri "heroku")
    "production"
    "development"))

(defn make-connection! []
  (let [env (env?)]
    (println "Environment:" env)
    (println "DB URI:" mongo-uri)
    (mg/connect-via-uri! mongo-uri)
    (if (= "production" env)
      (mg/use-db! "getclojure")
      (mg/use-db! "getclojure_development"))
    (mc/ensure-index "sexps" {:user 1})
    (mc/ensure-index "sexps" {:id 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-output 1})
    (mc/ensure-index "sexps" {:raw-value 1})
    {:environment env}))