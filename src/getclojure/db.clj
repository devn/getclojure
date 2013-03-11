(ns getclojure.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

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

(defn make-connection! []
  (let [mongo-uri (or (System/getenv "MONGOLAB_URI")
                      "mongodb://127.0.0.1/getclojure_development")
        env (env? mongo-uri)]
    (mg/connect-via-uri! mongo-uri)
    (make-indices)
    {:environment env :uri mongo-uri}))