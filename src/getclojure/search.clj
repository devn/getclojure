(ns getclojure.search
  (:require [clojure.string :as str]
            [getclojure.util :as util]))

(def special-characters
  ["\\" "+" "-" "&&" "||" "!" "(" ")"
   "{" "}" "[" "]" "^" "\"" "~"
   "*" "?" ":" "<" ">"])

(defn escape-character [cstring]
  (apply str (map #(str "\\" %) cstring)))

(defn encode-query [q]
  (loop [[c & r] special-characters
         out q]
    (if c
      (recur r (str/replace out c (escape-character c)))
      out)))

(defn search-sexps [q page-num]
  (info "Requested Query: " q)
  (let [offset (* (Integer/parseInt page-num) 25)
        encoded-query (encode-query q)
        q (if (empty? encoded-query)
            "iterate AND range"
            encoded-query)]
    (info "Encoded Query: " q)
    (esd/search "getclojure"
                "sexp"
                :query (q/query-string :query q
                                       :fields ["input^5" :value :output])
                :from offset
                :size 25)))

(defn get-num-hits [q page-num]
  (get-in (search-sexps q page-num) [:hits :total]))

(defn get-search-hits [result-map]
  (map :_source (get-in result-map [:hits :hits])))

(defn search-results-for [q page-num]
  (get-search-hits (search-sexps q page-num)))

(comment
  (require '[clojurewerkz.elastisch.rest :as esr])
  (esr/connect! "http://127.0.0.1:9200")
  (esi/delete "getclojure")
  (create-getclojure-index)
)
