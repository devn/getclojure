(ns getclojure.search
  (:require [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query :as q]
            [getclojure.util :as util]))

(defonce connect-elastisch (esr/connect! "http://127.0.0.1:9200"))

(def mappings
  {:sexp
   {:properties
    {:input {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:output {:type "string" :store "yes" :analyzer "clojure_code"}}
    {:value {:type "string" :store "yes" :analyzer "clojure_code"}}}})

(def clojure-analyzer
  {:clojure_code {:type "standard"
                  :filter ["standard" "lowercase" "stop"]
                  :stopwords ["(" ")" "[" "]" "{" "}" "#" "%"]}})

(defn create-getclojure-index []
  (when-not (esi/exists? "getclojure_development")
    (esi/create "getclojure_development"
                :settings {:index {:analysis {:analyzer clojure-analyzer}}}
                :mappings mappings)))

(defn add-to-index [env sexp-map]
  (esd/put env "sexp" (util/uuid) sexp-map))

(comment
  (esi/delete "getclojure_development")
  (create-getclojure-index)
  (esd/search "getclojure_development" "sexp" :query (q/text :input "test"))
  (esd/search "getclojure_development" "sexp" :query (q/text :input "let"))
  (esd/search "getclojure_development" "sexp" :query (q/fuzzy :input "let"))
  (esd/search "getclojure_development" "sexp" :query (q/fuzzy-like-this :input "let"))
)
