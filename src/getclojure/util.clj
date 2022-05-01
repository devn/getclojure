(ns getclojure.util
  (:require
   [clojure.string :as str])
  (:import (java.net URLEncoder)))

(defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [^String unencoded]
  (URLEncoder/encode unencoded "UTF-8"))

(defn generate-query-string
  "Provided a map of `params`, produces a url-encoded query string.

  Example:
  (generate-query-string {:q \"hi!\" :num 42})
  => \"q=hi%21&num=42\""
  [params]
  (str/join "&"
            (mapcat (fn [[k v]]
                      [(str (url-encode (name k))
                            "="
                            (url-encode (str v)))])
                    params)))

(defn truncate
  "Provided `n-chars` and `x`, truncates at `n-chars` by adding an ellipsis."
  [n-chars x]
  (if (> (count x) n-chars)
    (str (subs x 0 n-chars) "...")
    x))
