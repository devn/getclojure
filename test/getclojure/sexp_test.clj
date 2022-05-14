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
             (#'sut/run inc-expr 100)))

      (is (= {:input println-expr
              :output "\"100\\n\""
              :value "nil"}
             (#'sut/run println-expr 100)))

      (is (= {:input "(println \"1234567890\")"
              :output "\"1234..."
              :value "nil"}
             (#'sut/run "(println \"1234567890\")" 5))
          "Truncation happens at the specified length")

      (is (thrown? clojure.lang.ExceptionInfo
                   (#'sut/run spit-expr 10))))))

(deftest test-thunk-timeout
  (testing "thunk-timeout times out"
    (is (= 2
           (#'sut/thunk-timeout (fn [] (inc 1)) 10)))

    (is (= nil
           (#'sut/thunk-timeout (fn [] (Thread/sleep 15)) 10)))

    (is (= nil
           (#'sut/thunk-timeout (fn [] (throw (Exception. "Something went wrong."))) 10)))))

(deftest test-run-coll
  (testing "run-coll runs a collection of s-expression strings"
    (is (= [{:input "(inc 1)", :value "2", :output "\"\""}
            {:input "(inc 3)", :value "4", :output "\"\""}]
           (#'sut/run-coll 10 10 ["(inc 1)" "(inc 3)"])))))

(deftest remove-junk-sexps-test
  (let [input-sexps ["(doc +)" "(source +)" "(fn* [x] (inc x))" "(inc 1)"]]
    (testing "We remove things from the input expression set which aren't interesting"
      (is (= ["(inc 1)"]
             (#'sut/remove-junk-sexps input-sexps))))))

(deftest remove-junk-values-test
  (testing "We remove items where the value of the SCI run produces a function"
    (is (= [{:value "ok"}]
           (sut/remove-junk-values [{:value "#object[...]"}
                                    {:value "ok"}])))))

(deftest filtered-run-coll-test
  (let [input ["(fn [x])" "(source +)" "(fn* [x] (inc x))" "(inc 1)"]]
    (testing "Full filtering process around SCI"
      (is (= [{:input "(inc 1)"
               :value "2"
               :output "\"\""}]
             (sut/filtered-run-coll input))))))
