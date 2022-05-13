(ns getclojure.elastic
  (:require
   [clojure.java.io :as io]
   [ductile.conn :as es.conn]
   [ductile.document :as es.doc]
   [ductile.index :as es.index]
   [getclojure.config :as config]
   [outpace.config :refer (defconfig)]
   [schema.core :as s]
   [taoensso.timbre :as log]))

(defconfig elastic-url)
(defconfig index-name)
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

(s/defn create-index-when-not-exists! :- (s/maybe {s/Keyword s/Any})
  [conn :- clojure.lang.Delay
   index-name :- s/Str
   elastic-config]
  (when-not (es.index/index-exists? @conn index-name)
    (es.index/create! @conn index-name elastic-config)))

(s/defschema SearchResponse
  {:hits [{s/Keyword s/Any}]
   :total-pages s/Num
   :total-hits s/Num})

(s/defn search :- SearchResponse
  ([conn :- clojure.lang.Delay
    query-string :- s/Str]
   (search conn query-string 0))
  ([conn :- clojure.lang.Delay
    query-string :- s/Str
    page-num :- s/Num]
   (let [{:keys [data paging]} (es.doc/query @conn
                                             index-name
                                             {:query_string {:query query-string
                                                             :default_field "input"}}
                                             {:limit num-per-page
                                              :offset (* page-num num-per-page)})
         {:keys [total-hits]} paging]

     {:hits data
      :total-pages (long (Math/ceil (/ total-hits num-per-page)))
      :total-hits total-hits})))

(s/defn seed-sexp-coll
  [conn :- clojure.lang.Delay
   coll :- [{s/Keyword s/Any}]]
  (doseq [chunk (partition-all 1000 coll)]
    (es.doc/bulk @conn
                 {:create (mapv #(assoc % :_index index-name) chunk)}
                 {})))

(s/defn seed-sexps-from-file
  [conn :- clojure.lang.Delay]
  (let [sexps (read-string (slurp (io/resource "sexps/formatted-sexps.edn")))]
    (doseq [chunk (partition-all 1000 sexps)]
      (es.doc/bulk @conn
                   {:create (mapv #(assoc % :_index index-name) chunk)}
                   {}))))

(defn delete-and-recreate-index! [conn]
  (log/infof "\"%s\" index exists, deleting..." index-name)
  (when (es.index/index-exists? @conn index-name)
    (log/infof "\"%s\" index exists, deleting..." index-name)
    (es.index/delete! @conn index-name)
    (log/infof "Creating index \"%s\"..." index-name)
    (es.index/create! @conn index-name elastic-config)))

(defn -main
  [& _args]
  (log/info "Reseeding elasticsearch index from file...")

  (delete-and-recreate-index! conn)

  (log/info "Seeding s-expressions in elasticsearch...")
  (log/info (seed-sexps-from-file conn))

  (log/info "Completed seeding s-expressiong in elasticsearch")

  (shutdown-agents)
  (System/exit 0))


(comment
  #_(-> (search conn "iterate AND inc" 3))
  )
