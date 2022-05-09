(ns getclojure.config
  (:require
   [outpace.config :refer (defconfig)]))

(defconfig env)

(defn development? [] (= env "development"))
