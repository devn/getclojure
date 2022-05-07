(ns getclojure.elastic
  (:require
   [clojure.java.io :as io]
   [ductile.conn :as es.conn]
   [ductile.document :as es.doc]
   [ductile.index :as es.index]
   [ductile.schemas :as es.schemas]
   [schema.core :as s]
   [taoensso.timbre :as log]))

(def conn (delay (es.conn/connect {:host "localhost"
                                   :port 9207
                                   :version 7
                                   :protocol :http
                                   :timeout 20000
                                   :auth {:type :basic-auth
                                          :params {:user "elastic"
                                                   :pwd "ductile"}}})))

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

(s/defn create-index-when-not-exists :- (s/maybe {s/Keyword s/Any})
  [conn :- es.schemas/ESConn
   index-name :- s/Str
   elastic-config]
  (when-not (es.index/index-exists? @conn index-name)
    (es.index/create! @conn index-name elastic-config)))

(s/defschema SearchResponse
  {:hits [{s/Keyword s/Any}]
   :total-pages s/Num
   :total-hits s/Num})

(s/defn search :- SearchResponse
  ([conn :- es.schemas/ESConn
    query-string :- s/Str]
   (search conn query-string 0))
  ([conn :- es.schemas/ESConn
    query-string :- s/Str
    page-num :- s/Num]
   (let [num-per-page 50
         {:keys [data paging]} (es.doc/query @conn
                                             "getclojure_custom"
                                             {:query_string {:query query-string
                                                             :default_field "input"}}
                                             {:limit num-per-page
                                              :offset (* page-num num-per-page)})
         {:keys [total-hits]} paging]

     {:hits data
      :total-pages (long (Math/ceil (/ total-hits num-per-page)))
      :total-hits total-hits})))

#_(-> (search conn "iterate AND inc" 3))

(s/defn seed-sexps
  [conn :- es.schemas/ESConn]
  (let [formatted-sexps (map (fn [m x]
                               (assoc m :n x))
                             (read-string (slurp (io/resource "sexps/formatted-sexps.edn")))
                             (iterate inc 1))
        total-sexps (count formatted-sexps)]
    (doseq [doc formatted-sexps]
      (let [n (:n doc)]
        (when (= (mod n 1000) 0)
          (log/infof "Seeded %d/%d expressions" n total-sexps)))
      (es.doc/create-doc @conn
                         "getclojure_custom"
                         (dissoc doc :n)
                         {:refresh "false"}))))

(defn -main
  [& _args]
  (log/info "Reseeding elasticsearch index...")
  (es.index/delete! @conn "getclojure_custom")
  (create-index-when-not-exists conn "getclojure_custom" elastic-config)

  (log/info "Seeding s-expressions in elasticsearch...")
  (log/info (time (seed-sexps conn)))

  (log/info "Completed seeding s-expressiong in elasticsearch")
  (shutdown-agents)
  (System/exit 0))
