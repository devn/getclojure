(ns getclojure.test-helpers
  (:require
   [ductile.index :as es.index]
   [ductile.conn :as es.conn]
   [getclojure.elastic :as elastic]))

(defn elastic-fixture [f]
  (let [index "test-index"
        conn (es.conn/connect (elastic/make-conn))]
    (es.index/create! conn index elastic/elastic-config)
    (f)
    (es.index/delete! conn index)
    (es.conn/close conn)))
