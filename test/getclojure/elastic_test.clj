(ns getclojure.elastic-test
  (:require
   [clojure.test :refer (deftest testing is use-fixtures)]
   [ductile.conn :as es.conn]
   [getclojure.config]
   [getclojure.elastic :as sut]
   [schema.test :refer (validate-schemas)]
   [getclojure.test-helpers :as test-helpers]))

(use-fixtures :once validate-schemas)
(use-fixtures :once test-helpers/elastic-fixture)

(deftest parse-elastic-url-test
  (testing "It parses an elastic URL like the one we encounter from Bonsai on Heroku"
    (let [example-url "https://abcdefg:9abc392def4bd@pine-1234567.us-west-2.bonsaisearch.net:443"]
      (is (= {:host "pine-1234567.us-west-2.bonsaisearch.net"
              :port 443
              :pass "9abc392def4bd"
              :user "abcdefg"}
             (sut/parse-elastic-url example-url))))))

(deftest make-conn-test
  (with-redefs [getclojure.config/development? (constantly false)]
    (testing "It returns the the production config in non-development"
      (is (= :https
             (:protocol (sut/make-conn))))))

  (with-redefs [getclojure.config/development? (constantly true)]
    (testing "It returns the the development config in development"
      (is (= :http
             (:protocol (sut/make-conn)))))))

(defn mk-query-fn-response
  [coll]
  (let [total-hits (count coll)]
    (fn [_req]
      {:status 200
       :body {:hits {:hits (mapv (fn [m] {:_source m})
                                 coll)
                     :total {:value total-hits}}}
       :headers {:content-type "application/clojure"}})))

(defn mk-fake-conn
  [coll]
  (delay (es.conn/connect {:host "localhost"
                           :port 9200
                           :request-fn (mk-query-fn-response coll)})))

(deftest search-test
  (testing "It performs a search and returns what we expect"
    (is (= {:hits [{:field "fred"}]
            :total-hits 1
            :total-pages 1}
           (sut/search (mk-fake-conn [{:field "fred"}])
                       "fred"))))

  (with-redefs [sut/num-per-page 1]
    (testing "It returns the correct number of pages based on num-per-page"
      (is (= {:hits [{:field "fred"}
                     {:field "bob"}
                     {:field "wilma"}]
              :total-pages 3
              :total-hits 3}
             (sut/search (mk-fake-conn [{:field "fred"}
                                        {:field "bob"}
                                        {:field "wilma"}])
                         "fred OR bob OR wilma"
                         1))))))

(deftest ^:integration elastic-integration-test
  (with-redefs [sut/num-per-page 2
                sut/index-name "test-index"]
    (let [conn (delay (es.conn/connect (sut/make-conn)))
          sexps (mapv (fn [s] {:input s})
                      ["fred" "fred" "fred" "wilma" "barney" "betty" "+ - ~ * :"])
          _ (sut/seed conn sexps {:refresh "wait_for"})]
      (is (= {:hits [{:input "fred"}
                     {:input "fred"}]
              :total-pages 2
              :total-hits 3}
             (sut/search conn "fred" 0))
          "It returns the first page of fred results")

      (is (= {:hits [{:input "fred"}]
              :total-pages 2
              :total-hits 3}
             (sut/search conn "fred" 1))
          "It shows our third result for fred on the second page")

      (is (= {:hits []
              :total-pages 0
              :total-hits 0}
             (sut/search conn "bambam"))
          "It returns no results, 0 pages, and 0 hits when we can't find anything matching")

      (is (= {:hits [{:input "+ - ~ * :"}]
              :total-pages 1
              :total-hits 1}
             (sut/search conn "+ - ~ * :"))
          "It escapes query strings of special characters that may show up in a search"))))
