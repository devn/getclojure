(ns getclojure.format
  (:require [clojure.pprint :as pp]
            [clojail.core :refer [safe-read]]
            [me.raynes.conch :refer [let-programs]]))

(defn pygmentize [s]
  (let-programs [colorize "./pygmentize"
                 pwd "pwd"]
    (colorize "-fhtml" "-lclojure" {:in s :dir "resources/pygments"})))

;; TODO: There's a problem code-dispatch on the following:
;; (fn* [x] x). See http://dev.clojure.org/jira/browse/CLJ-1181
(defn print-with-code-dispatch [code]
  (if (.contains code "fn*")
    (with-out-str (pp/pprint (safe-read code)))
    (with-out-str
      (pp/with-pprint-dispatch pp/code-dispatch
        (pp/pprint (safe-read code))))))

(defn format-input [input]
  (if (string? input)
    (pygmentize (print-with-code-dispatch input))
    (pygmentize (str input "\n"))))

(defn format-value [value]
  (pygmentize value))

(defn format-output [output]
  (if-not (= output "\"\"")
    (pygmentize
      (safe-read
        (with-out-str
          (pp/with-pprint-dispatch pp/code-dispatch
            (pp/pprint output)))))))
