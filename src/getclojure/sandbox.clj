(ns getclojure.sandbox
  (:require [clojure.java.io :as io]))

(def sexp-db (set (read-string (slurp (io/file "working-sexps.db")))))
