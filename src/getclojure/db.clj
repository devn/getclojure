(ns getclojure.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defn make-connection! [uri]
  (let [environment (if (.contains uri "heroku") "production" "development")]
    (println "Environment:" environment)
    (println "DB URI:" uri)
    (mg/connect-via-uri! uri)
    (if (= "production" environment)
      (mg/use-db! "getclojure")
      (mg/use-db! "getclojure_development"))
    (mc/ensure-index "sexps" {:user 1})
    (mc/ensure-index "sexps" {:id 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-output 1})
    (mc/ensure-index "sexps" {:raw-value 1})
    {:environment environment}))