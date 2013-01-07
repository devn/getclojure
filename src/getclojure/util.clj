(ns getclojure.util)

(defn uuid []
  (str (java.util.UUID/randomUUID)))
