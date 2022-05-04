(ns getclojure.util
  (:require
   [clojure.string :as str]
   [schema.core :as s])
  (:import (java.net URLEncoder)))

(s/defn url-encode
  "Returns an UTF-8 URL encoded version of the given string."
  [unencoded :- s/Str]
  (URLEncoder/encode unencoded "UTF-8"))

(s/defn generate-query-string :- s/Str
  "Provided a map of `params`, produces a url-encoded query string.

  Example:
  (generate-query-string {:q \"hi!\" :num 42})
  => \"q=hi%21&num=42\""
  [params :- {s/Any s/Any}]
  (str/join "&"
            (mapcat (fn [[k v]]
                      [(str (url-encode (name k))
                            "="
                            (url-encode (str v)))])
                    params)))

(s/defn truncate :- s/Str
  "Provided `n-chars` and `x`, truncates at `n-chars` by adding an ellipsis."
  [n-chars :- s/Num
   x :- s/Str]
  (if (> (count x) n-chars)
    (str (subs x 0 n-chars) "...")
    x))
