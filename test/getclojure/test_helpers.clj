(ns getclojure.test-helpers
  (:require
   [clojure.test :as t]
   [ductile.index :as es.index]
   [ductile.conn :as es.conn]
   [getclojure.elastic :as elastic]))

(def test-index "test-index")

(defn elastic-fixture [f]
  (let [conn (es.conn/connect (elastic/make-conn))]
    (es.index/create! conn test-index elastic/elastic-config)
    (f)
    (es.index/delete! conn test-index)
    (es.conn/close conn)))
