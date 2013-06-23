(ns getclojure.search
  (:require [clojure.string :as string]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.index :as esi]
            [getclojure.util :as util]
            [taoensso.timbre :refer [info]]))

(def custom-analyzer
  {:custom_analyzer {:type "custom"
                     :tokenizer "custom_tokenizer"
                     :filter "custom_filter"}})

(def custom-filter
  {:custom_filter {:type "lowercase"}})

(def custom-tokenizer
  {:custom_tokenizer {:type "pattern"
                      :pattern "[\\s\\(\\)\\[\\]\\{\\}]+"}})

(def mappings
  {:sexp
   {:properties
    {:id {:type "integer" :store "yes"}
     :input {:type "string" :store "yes" :analyzer "custom_analyzer"}
     :output {:type "string" :store "yes" :analyzer "custom_analyzer"}
     :value {:type "string" :store "yes" :analyzer "custom_analyzer"}}}})

(defn create-getclojure-index []
  (when-not (esi/exists? "getclojure")
    (esi/create "getclojure"
                :settings {:index {:analysis {:analyzer custom-analyzer
                                              :tokenizer custom-tokenizer
                                              :filter custom-filter}}}
                :mappings mappings)))

(defn add-to-index [env sexp-map]
  (esd/put (name env) "sexp" (util/uuid) sexp-map))

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
      (recur r (string/replace out c (escape-character c)))
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
