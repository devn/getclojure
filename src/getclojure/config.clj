(ns getclojure.config
  (:require [clj-config.core :as cfg]))

(def config
  "External configuration"
  (cfg/safely cfg/read-config "config.clj"))