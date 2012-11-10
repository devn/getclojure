(ns getclojure.scrape
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]))

(def dates-url "http://clojure-log.n01se.net/date/")

(defn remote-logs []
  (map second (re-seq #"\>(.*\.html)\<" (apply str (slurp dates-url)))))

;; TODO: Use conch instead.
(def local-logs
  (filter #(re-find #"\.*\.html" (str %))
          (file-seq (io/file "logs"))))

(defn missing-logs []
  (set/difference
   (set (butlast (sort (remote-logs))))
   (set (map name local-logs))))

(defn get-missing-logs []
  (if (not (empty? missing-logs))
    (do (println ";;-> You are missing" (count missing-logs) "logs")
      (doseq [log missing-logs]
        (println ";;-> Downloading" log)
        (let [log-data (slurp (str dates-url log))]
          (spit (io/file "logs" log) log-data))))
    (println ";;-> You are currently up to date!")))
