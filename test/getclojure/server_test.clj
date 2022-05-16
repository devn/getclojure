(ns getclojure.server-test
  (:require
   [clojure.test :as test :refer (deftest testing is use-fixtures)]
   [getclojure.server :as sut]
   [schema.test :refer (validate-schemas)]))

(use-fixtures :once validate-schemas)
