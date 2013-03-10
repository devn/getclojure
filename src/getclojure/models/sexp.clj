(ns getclojure.models.sexp
  (:refer-clojure :exclude [sort find])
  (:require [monger.collection :as mc]
            [monger.core :as mg]
            [monger.query :refer [with-collection find sort limit paginate]]
            [getclojure.views.helpers :refer [pygmentize]]))

(let [mongolab (get (System/getenv) "MONGOLAB_URI" false)
      uri (or mongolab "mongodb://127.0.0.1/getclojure_development")]
  (mg/connect-via-uri! uri)
  ;; TODO: This should be pushed to somewhere more config-like.
  (if mongolab
    (mg/use-db! "getclojure")
    (mg/use-db! "getclojure_development"))
  (mc/ensure-index "sexps" {:user 1})
  (mc/ensure-index "sexps" {:id 1} {:unique true})
  (mc/ensure-index "sexps" {:raw-input 1} {:unique true})
  (mc/ensure-index "sexps" {:raw-output 1})
  (mc/ensure-index "sexps" {:raw-value 1}))

(def sexp-id
  "The current highest sexp-id."
  (atom
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
     :user (:id user)
     :raw-input input
     :raw-value value
     :raw-output output
     :input (pygmentize input)
     :value (pygmentize value)
     :output (pygmentize output)}))

(defn create-sexp!
  "Create a new sexp"
  [user sexp-map]
  (let [id (swap! sexp-id inc)
        sexp (sexp-record-map id user sexp-map)]
    (if-not (sexp-exists? sexp)
      (mc/insert-and-return "sexps" sexp))))