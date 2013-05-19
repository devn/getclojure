(ns getclojure.util
  (:require [clojure.string :as str]
            [markdown.core :as md]
            [noir.io :as io])
  (:import java.net.URLEncoder))

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(defn format-time
  "formats the time using SimpleDateFormat, the default format is
   \"dd MMM, yyyy\" and a custom one can be passed in as the second argument"
  ([time] (format-time time "dd MMM, yyyy"))
  ([time fmt]
     (.format (new java.text.SimpleDateFormat fmt) time)))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (md/md-to-html-string (io/slurp-resource filename)))

;; Web Utils
(defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [unencoded]
  (URLEncoder/encode unencoded "UTF-8"))

(defn generate-query-string [params]
  (str/join "&"
            (mapcat (fn [[k v]]
                      (if (sequential? v)
                        (map #(str (url-encode (name %1))
                                   "="
                                   (url-encode (str %2)))
                             (repeat k) v)
                        [(str (url-encode (name k))
                              "="
                              (url-encode (str v)))]))
                    params)))

(defn inclusive-range
  ([] (inclusive-range 0 Double/POSITIVE_INFINITY 1))
  ([end] (inclusive-range 0 end 1))
  ([start end] (inclusive-range start end 1))
  ([start end step] (range start (inc end) step)))
