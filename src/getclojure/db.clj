(ns getclojure.db
  (:require
   [outpace.config :refer [defconfig]]
   [next.jdbc :as jdbc]))

(defconfig db-url)

(def db (delay (jdbc/get-datasource {:jdbcUrl db-url})))

(jdbc/execute! @db ["SELECT 1 as cool;"])
