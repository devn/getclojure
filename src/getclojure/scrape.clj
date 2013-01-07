(ns getclojure.scrape
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]))

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
   (set (map #(str/replace % #"logs\/" "") local-logs))))

(defn get-missing-log [lf]
  (let [log-data (slurp (str dates-url lf))]
    (spit (io/file "logs" lf) log-data)))

(defn get-missing-logs []
  (let [missing (missing-logs)]
   (if-not (empty? missing)
     (do (println ";;-> You are missing" (count missing) "logs")
         (doseq [log missing]
           (println ";;-> Downloading" log)
           (get-missing-log log)))
     (println ";;-> You are currently up to date!"))))
