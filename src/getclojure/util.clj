(ns getclojure.util
  (:require [clojure.string :as str])
  (:import java.net.URLEncoder))

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [^String unencoded]
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

(defn truncate
  "Provided `n-chars` and `x`, truncates at `n-chars` by adding an ellipsis."
  [n-chars x]
  (if (> (count x) n-chars)
    (str (subs x 0 n-chars) "...")
    x))
