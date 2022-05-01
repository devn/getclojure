(ns getclojure.extract-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest testing is] :as test]
   [getclojure.extract :as subject]
   [schema.test]))

(test/use-fixtures :once schema.test/validate-schemas)

(deftest extract-sexps-test
  (testing "We extract s-expressions from strings as expected."
    (is (= '()
           (subject/extract-sexps "")))

    (is (= '()
           (subject/extract-sexps nil)))

    (is (= (into #{} ["(inc 1)" "(inc 2)" "()"])
           (into #{} (subject/extract-sexps "(inc 1) blah blah () (inc 2)"))))

    (is (= (into #{} ["(+ (inc 1))" "(inc 2)"])
           (into #{} (subject/extract-sexps "(+ (inc 1)) (inc 2)"))))))

(deftest logfile->mapseq-test
  (testing "logfile->mapseq returns maps with expected data for our sample html file"
    (let [logfile (io/as-file (io/resource "logs/2017-06-29.html"))
          result (subject/logfile->mapseq logfile)]

      (is (every? (fn [m] (some? (:nickname m)))
                  result)
          "Every line contains a nickname despite some entries missing them")

      (is (every? (fn [m] (some? (:content m)))
                  result)
          "Every line contains content")

      (is (every? (fn [m] (some? (:timestamp m)))
                  result)
          "Every line contains a timestamp")

      (is (every? (fn [m] (some? (:date m)))
                  result)
          "Every line contains a date")

      (is (= 1 (-> (filter (fn [m] (:sexps m)) result)
                   count))
          "Only one line in the log contains s-expressions")

      (is (= 3 (-> (filter (fn [m] (:sexps m)) result)
                   first
                   :sexps
                   count))
          "The line containing s-expressions has 3 expressions"))))

(deftest forward-propagate-test
  (testing "That we fill nil values with the last seen value for the same keyword"
    (let [example-input [{:nickname "bill" :content "ciil"}
                         {:nickname nil :content "cool*"}]
          empty-start [{:nickname nil :content "a"}
                       {:nickname "ted" :content "b"}]]
      (is (= [{:nickname "bill"
               :content "ciil"}
              {:nickname "bill"
               :content "cool*"}]
             (subject/forward-propagate example-input :nickname))
          "We carry forward the nickname to the next nil value")
      (is (= [{:nickname nil
               :content "a"}
              {:nickname "ted"
               :content "b"}]
             (subject/forward-propagate empty-start :nickname))
          "If we start with a nil value, we don't clobber the next entry's value")
      (is (= [{:nickname "bill"
               :content "ciil"}
              {:nickname "bill"
               :content "cool*"}
              {:nickname "bill"
               :content "how is everyone?"}]
             (subject/forward-propagate (conj example-input {:nickname nil
                                                             :content "how is everyone?"})
                                        :nickname))
          "We carry forward the nickname to all future nil entries, not just the next one"))))
