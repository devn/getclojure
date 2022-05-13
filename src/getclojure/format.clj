(ns getclojure.format
  (:require
   [clojure.string :as str]
   [libpython-clj2.require :refer [require-python]]
   [zprint.core :as zp]))

(require-python 'pygments)
(require-python 'pygments.lexers)
(require-python 'pygments.formatters)

(defn ^:private pygmentize
  [s]
  (pygments/highlight s
                      (pygments.lexers/get_lexer_by_name "Clojure")
                      (pygments.formatters/get_formatter_by_name "html")))

(defn input
  [s]
  (binding [*read-eval* false]
    (pygmentize (zp/zprint-str s {:parse-string? true}))))

(defn value
  [s]
  (binding [*read-eval* false]
    (if-not (str/ends-with? s "...")
      (pygmentize (zp/zprint-str s {:parse-string? true}))
      (pygmentize s))))

(defn output
  [s]
  (binding [*read-eval* false]
    (when-not (= s "\"\"")
      (pygmentize (read-string (zp/zprint-str s))))))
