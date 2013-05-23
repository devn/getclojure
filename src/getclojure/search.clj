(ns getclojure.search
  (:require [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.index :as esi]
            [getclojure.util :as util]))

(def custom-analyzer
  {:custom_analyzer {:type "custom"
                     :tokenizer "custom_tokenizer"
                     :filter []}})

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
                                              :tokenizer custom-tokenizer}}}
                :mappings mappings)))

(defn add-to-index [env sexp-map]
  (esd/put (name env) "sexp" (util/uuid) sexp-map))

;; :from, :size
(defn search-sexps [q page-num]
  (let [offset (* (Integer/parseInt page-num) 25)
        query (if (empty? q) "comp AND juxt" q)]
    (esd/search "getclojure"
                "sexp"
                :query (q/dis-max :queries [(q/term :input query)
                                            (q/text :input query)
                                            (q/fuzzy-like-this-field :input {:like_text query})]
                                  :boost 1.2
                                  :tie_breaker 0.7)
                :from offset
                :size 25)))

(defn get-num-hits [q page-num]
  (get-in (search-sexps q page-num) [:hits :total]))

(defn get-search-hits [result-map]
  (map :_source (get-in result-map [:hits :hits])))

(defn search-results-for [q page-num]
  (get-search-hits (search-sexps q page-num)))

(comment
  (esr/connect! "http://127.0.0.1:9200")
  (esi/delete "getclojure")
  (create-getclojure-index)
)
