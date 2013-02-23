(ns getclojure.views.helpers
  (:use [clojail.core :only (safe-read)]
        [hiccup.util :only (escape-html)])
  (:require [clojure.pprint :as pp]))

(defn print-with-code-dispatch [code]
  (with-out-str
    (pp/with-pprint-dispatch pp/code-dispatch
      (pp/pprint (safe-read code)))))

(defn format-input [input]
  (if (string? input)
    [:pre.input.prettyprint.lang-clj (print-with-code-dispatch input)]
    [:pre.input.prettyprint.lang-clj (str input "\n")]))

(defn format-value [value]
  [:pre.value.prettyprint.lang-clj (escape-html value)])

(defn format-output [output]
  (if-not (= output "\"\"")
    [:pre.output.prettyprint.lang-clj (safe-read
                                       (with-out-str
                                         (pp/with-pprint-dispatch pp/code-dispatch
                                           (pp/pprint output))))]))
