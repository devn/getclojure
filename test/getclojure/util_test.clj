(ns getclojure.util-test
  (:require
   [clojure.test :refer (deftest testing is)]
   [getclojure.util :as sut]))

(deftest test-truncate
  (testing "It truncates strings"
    (is (= "abc"
           (sut/truncate 3 "abc"))
        "Does not truncate when the string is of a length equal to the truncation value")
    (is (= "ab..."
           (sut/truncate 2 "abc"))
        "Truncates when the string is longer than the truncation value")
    (is (= "abc"
           (sut/truncate 4 "abc"))
        "Does not truncate when the string smaller the truncation value")))
