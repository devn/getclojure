(ns getclojure.elastic
  (:require
   [clojure.string :as str]
   [ductile.conn :as es.conn]
   [ductile.document :as es.doc]
   [ductile.index :as es.index]
   [getclojure.config :as config]
   [outpace.config :refer (defconfig)]
   [schema.core :as s]
   [taoensso.timbre :as log]))

(defconfig elastic-url "https://abc123:def456@example.com:443")
(defconfig index-name "getclojure")
(def num-per-page 50)

(s/defschema ParsedElasticURL
  {:host s/Str
   :user s/Str
   :port s/Num
   :pass s/Str})

(s/defn parse-elastic-url :- ParsedElasticURL
  [url :- s/Str]
  (let [[_ user pass host port] (re-matches #"https://(\w+):(\w+)@(.*):(\d+)" url)]
    {:host host
     :user user
     :port (Integer/parseInt port)
     :pass pass}))

(s/defn make-conn
  []
  (let [base-conn {:host "localhost"
                   :port 9207
                   :version 7
                   :protocol :http
                   :timeout 20000
                   :auth {:type :basic-auth
                          :params {:user "elastic"
                                   :pwd "ductile"}}}]
    (if (config/development?)
      base-conn
      (let [{:keys [host user pass port]} (parse-elastic-url elastic-url)]
        (-> base-conn
            (update-in [:host] (constantly host))
            (update-in [:port] (constantly port))
            (update-in [:protocol] (constantly :https))
            (update-in [:auth :params :user] (constantly user))
            (update-in [:auth :params :pwd] (constantly pass)))))))

(def conn (delay (es.conn/connect (make-conn))))

(def elastic-config {:settings {:number_of_shards 5
                                :number_of_replicas 1
                                :refresh_interval "1s"
                                :analysis {:analyzer {"custom_analyzer" {:type "custom"
                                                                         :tokenizer "custom_tokenizer"
                                                                         :filter ["lowercase"]}}
                                           :tokenizer {"custom_tokenizer" {:type "pattern"
                                                                           :pattern "[\\s\\(\\)\\[\\]\\{\\}]+"}}}}
                     :mappings {:properties {:input {:type :text :analyzer "custom_analyzer"}
                                             :value {:type :text :analyzer "custom_analyzer"}
                                             :output {:type :text :analyzer "custom_analyzer"}
                                             :formatted-input {:type :text :index false}
                                             :formatted-value {:type :text :index false}
                                             :formatted-output {:type :text :index false}}}})

(s/defschema SearchResponse
  {:hits [{s/Keyword s/Any}]
   :total-pages s/Num
   :total-hits s/Num})

(s/defn escape-query-chars :- s/Str
  "Given a query string, escapes a subset of the special characters that overlap
  with valid clojure searches."
  [query-string :- s/Str]
  (let [special-chars ["+" "*" ":" "-" "~"]]
    (loop [[character & xs] special-chars
           qstring query-string]
      (if character
        (recur xs (str/replace qstring character (str "\\" character)))
        qstring))))

(s/defn search :- SearchResponse
  "Provided a connection and query string, searches for records matching the
  query string."
  ([conn :- clojure.lang.Delay
    query-string :- s/Str]
   (search conn query-string 0))
  ([conn :- clojure.lang.Delay
    query-string :- s/Str
    page-num :- s/Num]
   (let [{:keys [data paging]} (es.doc/query @conn
                                             index-name
                                             {:query_string {:query (escape-query-chars query-string)
                                                             :default_field "input"}}
                                             {:limit num-per-page
                                              :offset (* page-num num-per-page)})
         {:keys [total-hits]} paging]

     {:hits data
      :total-pages (long (Math/ceil (/ total-hits num-per-page)))
      :total-hits total-hits})))

(s/defn delete-and-recreate-index!
  "Provided a connection, deletes and recreates `index-name` if it exists."
  [conn :- clojure.lang.Delay]
  (when (es.index/index-exists? @conn index-name)
    (log/infof "\"%s\" index exists, deleting..." index-name)
    (es.index/delete! @conn index-name)
    (log/infof "Creating index \"%s\"..." index-name)
    (es.index/create! @conn index-name elastic-config)))

(s/defn seed
  "Provided a connection and a collection of maps, does a bulk create"
  ([conn :- clojure.lang.Delay
    coll :- [{s/Keyword s/Any}]]
   (seed conn coll {}))
  ([conn :- clojure.lang.Delay
    coll :- [{s/Keyword s/Any}]
    opts :- {s/Keyword s/Any}]
   (log/info "Seeding elaticsearch...")
   (doseq [chunk (partition-all 1000 coll)]
     (es.doc/bulk @conn
                  {:create (mapv #(assoc % :_index index-name) chunk)}
                  opts))))

(comment
  (-> (search conn "iterate AND inc" 0))
  )
