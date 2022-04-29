(ns getclojure.scrape
  (:require
   [clojure.java.io :as io]))

(defn local-logs
  "Returns a collection of HTML files in the resources/logs directory."
  []
  (filter #(re-find #"\.*\.html" (str %))
          (file-seq (io/as-file (io/resource "logs")))))
