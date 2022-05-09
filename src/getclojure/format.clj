(ns getclojure.format
  (:require
   [clojure.pprint :as pp]
   [libpython-clj2.require :refer [require-python]]))

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
    (pygmentize (with-out-str s
                  (pp/with-pprint-dispatch pp/code-dispatch
                    (pp/pprint (read-string s)))))))

(defn value
  [s]
  (pygmentize s))

(defn output
  [s]
  (binding [*read-eval* false]
    (when-not (= s "\"\"")
      (pygmentize
       (read-string
        (with-out-str (pp/with-pprint-dispatch pp/code-dispatch
                        (pp/pprint s))))))))
