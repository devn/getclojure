(ns getclojure.sandbox
  (:require [clojure.java.io :as io]))

(def sexp-db (into #{} (read-string (slurp (io/file "working-sexps.db")))))
