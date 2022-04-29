(ns getclojure.sexp-test
  (:require
   [clojure.test :refer (deftest testing is)]
   [getclojure.sexp :as sut]))

(deftest test-run
  (testing "We capture input, output, and value for s-expressions"
    (let [inc-expr "(inc 1)"
          println-expr "(println 100)"
          spit-expr "(spit \"f.txt\" 42)"]

      (is (= {:input inc-expr
              :output "\"\""
              :value "2"}
             (#'sut/run inc-expr)))

      (is (= {:input println-expr
              :output "\"100\\n\""
              :value "nil"}
             (#'sut/run println-expr)))

      (is (thrown? clojure.lang.ExceptionInfo
                   (#'sut/run spit-expr))))))
