(ns getclojure.db
  (:require [monger.collection :as mc]
            [monger.core :as mg]))

(defn env? [mongo-uri]
  (if (.contains mongo-uri "heroku")
    :production
    :development))

(def mongo-uri (or (System/getenv "GETCLOJURE_PRODUCTION_MONGO")
                   "mongodb://127.0.0.1/getclojure"))

(def env (env? mongo-uri))

(def db (delay (mg/connect-via-uri mongo-uri)))

(defn- make-indices []
  (mc/ensure-index @db "sexps" {:user 1})
  (mc/ensure-index @db "sexps" {:id 1} {:unique true})
  (mc/ensure-index @db "sexps" {:raw-input 1} {:unique true})
  (mc/ensure-index @db "sexps" {:raw-output 1})
  (mc/ensure-index @db "sexps" {:raw-value 1}))

(defn clean-db! []
  (mc/remove @db "getclojure")
  (mc/remove @db "users"))

(defn make-connection! []
  (make-indices)
  @db
  {:environment env
   :uri mongo-uri})
