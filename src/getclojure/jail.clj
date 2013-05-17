(ns getclojure.jail
  (:require [clojail.core :as jail]
            [clojail.testers :as tester])
  (:import (java.io StringWriter)))

;; print-method added due to 2008-11-09
(def ^{:private true} getclojure-tester
  (conj tester/secure-tester
        (tester/blacklist-symbols '#{print-method})
        (tester/blacklist-packages ["java.util.regex.Pattern"])))

(def sb (jail/sandbox getclojure-tester :timeout 1000))

(defn truncate [x]
  (if string?
    (let [out (apply str (take 400 x))]
      (if (> (count x) 400)
        (str out "...")
        out))
    x))

(defn run-sexp-in-sandbox
  [sexp]
  (with-open [writer (StringWriter.)]
    (let [bindings {#'*out* writer}
          e (jail/safe-read sexp)
          v (sb e bindings)
          o (str writer)]
      {:input sexp
       :value (truncate (pr-str v))
       :output (truncate (pr-str o))})))
