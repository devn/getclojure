(ns getclojure.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(defn make-connection! []
  (let [mongolab (get (System/getenv) "MONGOLAB_URI" false)
        db-uri (or mongolab "mongodb://127.0.0.1/getclojure_development")]
    (mg/connect-via-uri! db-uri)
    (if mongolab
      (mg/use-db! "getclojure")
      (mg/use-db! "getclojure_development"))
    (mc/ensure-index "sexps" {:user 1})
    (mc/ensure-index "sexps" {:id 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
    (mc/ensure-index "sexps" {:raw-output 1})
    (mc/ensure-index "sexps" {:raw-value 1})
    {:environment (if mongolab "production" "development")}))