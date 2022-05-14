(ns getclojure.format-test
  (:require
   [clojure.test :refer (deftest testing is use-fixtures)]
   [getclojure.format :as sut]
   [schema.test :refer (validate-schemas)]))

(use-fixtures :once validate-schemas)

(deftest formatting-test
  (testing "It formats inputs, outputs, and values as HTML strings"
    (is (= "<div class=\"highlight\"><pre><span></span><span class=\"p\">(</span><span class=\"kd\">defn </span><span class=\"nv\">foo</span><span class=\"w\"> </span><span class=\"p\">[</span><span class=\"nv\">x</span><span class=\"p\">]</span><span class=\"w\"> </span><span class=\"p\">(</span><span class=\"nf\">bar</span><span class=\"w\"> </span><span class=\"nv\">baz</span><span class=\"w\"> </span><span class=\"nv\">qux</span><span class=\"w\"> </span><span class=\"p\">{</span><span class=\"ss\">:a</span><span class=\"w\"> </span><span class=\"mi\">1</span>,<span class=\"w\"> </span><span class=\"ss\">:b</span><span class=\"w\"> </span><span class=\"mi\">2</span><span class=\"p\">}))</span><span class=\"w\"></span>\n</pre></div>\n"
           (sut/input "(defn foo [x] (bar baz qux {:a 1 :b 2}))")))

    (is (= "<div class=\"highlight\"><pre><span></span><span class=\"mi\">2</span><span class=\"w\"></span>\n</pre></div>\n"
           (sut/value "2")))

    (is (= "<div class=\"highlight\"><pre><span></span><span class=\"s\">&quot;Hello, world!&quot;</span><span class=\"w\"></span>\n</pre></div>\n"
           (sut/output "\"Hello, world!\"")))

    (is (= nil
           (sut/output "\"\""))
        "Return nothing if we have no output")))
