(ns getclojure.seed-test
  (:require
   [clojure.test :refer (deftest testing is use-fixtures)]
   [ductile.conn :as es.conn]
   [ductile.document :as es.doc]
   [getclojure.config :as config]
   [getclojure.elastic :as elastic]
   [getclojure.seed :as sut]
   [getclojure.test-helpers :as test-helpers]
   [schema.test :refer (validate-schemas)]))

(use-fixtures :once validate-schemas)
(use-fixtures :once test-helpers/elastic-fixture)

(deftest ^:integration seed-expressions
  (testing "Full ETL and search"
    (with-redefs [elastic/index-name "test-index"
                  config/env "test"]
      (let [index "test-index"
            conn (delay (es.conn/connect (elastic/make-conn)))
            _ (sut/seed 1 {:refresh "wait_for"})]
        (is (= 3
               (es.doc/count-docs @conn index)))
        (is (= {:hits
                [{:input "(inc 1)"
                  :value "2"
                  :output "\"\""
                  :formatted-input
                  "<div class=\"highlight\"><pre><span></span><span class=\"p\">(</span><span class=\"nb\">inc </span><span class=\"mi\">1</span><span class=\"p\">)</span><span class=\"w\"></span>\n</pre></div>\n"
                  :formatted-value
                  "<div class=\"highlight\"><pre><span></span><span class=\"mi\">2</span><span class=\"w\"></span>\n</pre></div>\n"
                  :formatted-output nil}]
                :total-pages 1
                :total-hits 1}
               (elastic/search conn "inc")))))))
