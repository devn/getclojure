(ns getclojure.db
  (:require [monger.collection :as mc]
            [monger.core :as mg]))

(defn env? [mongo-uri]
  (if (.contains mongo-uri "heroku")
    :production
    :development))

(defn- make-indices []
  (mc/ensure-index "sexps" {:user 1})
  (mc/ensure-index "sexps" {:id 1} {:unique true})
  (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
  (mc/ensure-index "sexps" {:raw-output 1})
  (mc/ensure-index "sexps" {:raw-value 1}))

(defn clean-db! []
  (mc/remove "getclojure")
  (mc/remove "users"))

(defn make-connection! []
  (let [mongo-uri (or (System/getenv "GETCLOJURE_PRODUCTION_MONGO")
                      "mongodb://127.0.0.1/getclojure")
        env (env? mongo-uri)]
    (mg/connect-via-uri! mongo-uri)
    (make-indices)
    {:environment env :uri mongo-uri}))
