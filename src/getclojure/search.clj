(ns getclojure.search
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query :as q]
            [getclojure.util :as util]))

(def mappings
  {:sexp
   {:properties
    {:input {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:output {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:value {:type "string" :store "yes" :analyzer "clojure_code"}}}})

(def clojure-analyzer
  {:clojure_code {:type "custom"
                  :tokenizer "lowercase"
                  :stopwords [" "]
                  :filter ["lowercase" "stop"]}})

(defn create-getclojure-index []
  (when-not (esi/exists? "getclojure")
    (esi/create "getclojure"
                :settings {:index {:analysis {:analyzer clojure-analyzer}}}
                :mappings mappings)))

(defn add-to-index [env sexp-map]
  (esd/put (name env) "sexp" (util/uuid) sexp-map))

;; :from, :size
(defn search-sexps [q page-num]
  (let [offset (* page-num 25)]
    (esd/search "getclojure"
                "sexp"
                :query (q/query-string :query q
                                       :allow_leading_wildcard true
                                       :default_operator "AND")
                :from offset
                :size 25)))

(defn get-search-hits [result-map]
  (map :_source (get-in result-map [:hits :hits])))

(defn search-results-for
  ([q page-num] (get-search-hits (search-sexps q page-num))))

(comment
  (esr/connect! "http://6ooyks68:mijf5fy0wrca3gmh@oak-8299758.us-east-1.bonsai.io")
  (esr/connect! "http://127.0.0.1:9200")
  (esr/connect! "url_for_elasticsearch")
  (esi/delete "getclojure")
  (create-getclojure-index)
  (take 3 (map :_source  (get-in (esd/search "getclojure" "sexp" :query (q/text :input "foo")) [:hits :hits])))
  (esd/search "getclojure_development" "sexp" :query (q/text :input "let"))
  (esd/search "getclojure_development" "sexp" :query (q/fuzzy :input "let"))
  (esd/search "getclojure_development" "sexp" :query (q/fuzzy-like-this :input "let"))
)