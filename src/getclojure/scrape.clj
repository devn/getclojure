(ns getclojure.scrape
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [getclojure.extract :as e]
            [cd-client.core :as cd]))

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

;; ClojureDocs
(cd/set-local-mode! (io/file "clojuredocs-snapshot-latest.txt"))

(def core-publics
  (map first (ns-publics 'clojure.core)))

(defn- remove-markdown
  "Remove basic markdown syntax from a string."
  [text]
  (-> text
      (.replaceAll "<pre>" "")
      (.replaceAll "</pre>" "")
      (.replaceAll "<code>" "")
      (.replaceAll "</code>" "")
      (.replaceAll "<b>" "")
      (.replaceAll "</b>" "")
      (.replaceAll "<p>" "")
      (.replaceAll "</p>" "")
      (.replaceAll "&gt;" "")
      (.replaceAll "&lt;" "")
      (.replaceAll "&amp;" "")
      (.replaceAll "<br>" "")
      (.replaceAll "<br/>" "")
      (.replaceAll "<br />" "")
      (.replaceAll "\\\\r\\\\n" "")))

(def clojuredocs-sexp-harvest (atom []))

(defn get-sexps-from-clojuredocs []
  (doseq [sexps (map #(e/extract-sexps (str/replace (remove-markdown (:body %)) #"\s+" " "))
                     (mapcat #(:examples (cd/examples "clojure.core" %))
                             core-publics))]
    (doseq [sexp sexps]
      (swap! clojuredocs-sexp-harvest conj sexp))))