(ns getclojure.elastic
  (:require
   [ductile.conn :as es.conn]
   [ductile.document :as es.doc]
   [ductile.index :as es.index]
   [taoensso.timbre :as log]
   [clojure.java.io :as io]))

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

(defn create-index!
  [conn index-name elastic-config]
  (when-not (es.index/index-exists? @conn index-name)
    (es.index/create! @conn index-name elastic-config)))

#_(create-index! conn "getclojure_custom" elastic-config)

#_(es.index/delete! @conn "getclojure_custom")

(defn search [conn query-string]
  (es.doc/query @conn
                "getclojure_custom"
                {:query_string {:query query-string
                                :default_field "input"}}
                {:full-hits? true
                 :limit 20
                 :offset 0}))

#_(-> (search conn "iterate AND inc") :data count)

(defn seed-sexps
  [conn]
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
                         doc
                         {:refresh "false"}))))

(defn -main
  [& _args]
  (log/info "Reseeding elasticsearch index...")
  (es.index/delete! @conn "getclojure_custom")
  (create-index! conn "getclojure_custom" elastic-config)

  (log/info "Seeding s-expressions in elasticsearch...")
  (log/info (time (seed-sexps conn)))

  (log/info "Completed seeding s-expressiong in elasticsearch")
  (shutdown-agents)
  (System/exit 0))
