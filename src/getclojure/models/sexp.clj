(ns getclojure.models.sexp
  (:require [monger.collection :as mc]
            [getclojure.format :refer [format-input
                                       format-output
                                       format-value]]
            [monger.query :refer [find limit sort with-collection]])
  (:refer-clojure :exclude [sort find]))

(def sexp-id (atom 0))

(defn set-highest-sexp-id! []
  (reset! sexp-id
          (-> (with-collection "sexps"
                (find {})
                (sort {:id -1})
                (limit 1))
              first
              :id
              (or 0))))

(defn sexp-exists? [{:keys [raw-input]}]
  (mc/any? "sexps" {:raw-input raw-input}))

(defn sexp-record-map [id user sexp-map]
  (let [{:keys [input value output]} sexp-map]
    {:id id
     :user (:_id user)
     :raw-input input
     :raw-value value
     :raw-output output
     :input (format-input input)
     :value (format-value value)
     :output (format-output output)}))

(defn create-sexp!
  "Create a new sexp"
  [user sexp-map]
  (let [id (swap! sexp-id inc)
        sexp (sexp-record-map id user sexp-map)]
    (if-not (sexp-exists? sexp)
      (mc/insert-and-return "sexps" sexp))))
