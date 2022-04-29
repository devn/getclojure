(ns getclojure.extract-test
  (:require
   [clojure.test :refer [deftest is]]
   [getclojure.extract :as subject]))

(deftest extract-sexps-test
  (is (= '()
         (subject/extract-sexps "")))

  (is (= (into #{} ["(inc 1)" "(inc 2)" "()"])
         (into #{} (subject/extract-sexps "(inc 1) blah blah () (inc 2)"))))

  (is (= (into #{} ["(+ (inc 1))" "(inc 2)"])
         (into #{} (subject/extract-sexps "(+ (inc 1)) (inc 2)"))))

  (is (= '()
         (subject/extract-sexps nil))))
