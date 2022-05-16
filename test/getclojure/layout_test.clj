(ns getclojure.layout-test
  (:require
   [clojure.test :refer (deftest testing is use-fixtures)]
   [getclojure.layout :as sut]
   [schema.test :refer (validate-schemas)]))

(use-fixtures :once validate-schemas)

(deftest calculate-pagination-test
  (testing "It calculates pagination parameters correctly"
    (is (= false
           (:show-pagination? (sut/calculate-pagination "0" 0)))
        "Suppress pagination when there are no pages")

    (is (= true
           (:show-pagination? (sut/calculate-pagination "0" 2)))
        "Show pagination when there is more than 1 page")

    (is (= false
           (:show-previous-links? (sut/calculate-pagination "0" 0)))
        "Suppress previous links when there is only one page")

    (is (= true
           (:show-previous-links? (sut/calculate-pagination "0" 2)))
        "Show previous links when there is more than one page")

    (is (= 0
           (:first-page (sut/calculate-pagination "0" 0)))
        "The first page is always 0")

    (is (= 0
           (:previous-page (sut/calculate-pagination "-1" 3)))
        "The previous page does not go out of bounds")

    (is (= 1
           (:previous-page (sut/calculate-pagination "2" 3)))
        "The previous page is one previous to the current page number")

    (is (= (range 0 10)
           (:page-numbers-to-show (sut/calculate-pagination "0" 10)))
        "It computes the number of pages to show")

    (is (= [8 9]
           (:page-numbers-to-show (sut/calculate-pagination "8" 10)))
        "It computes the number of pages to show based on what page you're on")

    (is (= true
           (:show-next-links? (sut/calculate-pagination "0" 12)))
        "Show next links when there are more pages")

    (is (= false
           (:show-next-links? (sut/calculate-pagination "0" 1)))
        "Suppress next links when there is zero or one page")

    (is (= false
           (:show-next-links? (sut/calculate-pagination "4" 5)))
        "Suppress next links if we're on the last page")

    (is (= 5
           (:last-page (sut/calculate-pagination "0" 6)))
        "The last page is one less than the total number of pages (zero-indexed)")))
