(ns getclojure.models.user
  (:refer-clojure :exclude [sort find])
  (:use [validateur.validation])
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as s]
            [getclojure.config :refer [config]]
            [getclojure.util :refer [inclusive-range]]
            [monger.collection :as mc]
            [monger.query :refer [with-collection find sort paginate]]
            [noir.session :as session])
  (:import org.bson.types.ObjectId))

;; TODO: It would be nice to have uniqueness-of in here
(def validate-user-map
  (validation-set
   (length-of :username :within (inclusive-range 3 16))
   (format-of :username :format #"^[a-zA-Z0-9_]*$")))

(defn unique-user? [username]
  (nil? (mc/find-one-as-map "users" {:username username})))

(defn create-user! [email username]
  (let [name (s/lower-case username)
        qmap {:email email
              :username name}
        errors (validate-user-map qmap)]
    (if (and (unique-user? name) (empty? errors))
      (let [user (mc/insert-and-return "users" qmap)]
        ;;(session/put! :user (assoc qmap :id (str (:_id user))))
        user)
      errors)))

(defn user-exists [email]
  (when-let [{:keys [username _id]} (mc/find-one-as-map "users" {:email email})]
    (session/put! :user {:email email
                         :username username
                         :id (str _id)})
    username))

(defn verify-host [host hosts]
  (-> (.split host ":")
      (first)
      (hosts)))

(defn get-hosts []
  (if-let [hosts (System/getenv "HOSTS")]
    (set (.split hosts ","))
    (or (:hosts config) #{"localhost"})))

(defn verify-assertion [host assertion]
  (let [verified (json/parse-string
                  (:body
                   (http/post "https://browserid.org/verify"
                              {:form-params
                               {:assertion assertion
                                :audience (verify-host host (get-hosts))}}))
                  true)]
    (when (= "okay" (:status verified))
      verified)))

;; Queries
(defn get-user [username]
  (mc/find-one-as-map "users" {:username username}))

(defn get-user-by-id [id]
  (mc/find-map-by-id "users" (ObjectId. id)))

(defn user-sexps [username page & [others]]
  (with-collection "sexps"
    (find (merge {:username (str (:_id (get-user username)))} others))
    (sort {:date -1})
    (paginate :page page :per-page 10)))

(defn count-user-sexps [username & [others]]
  (mc/count "sexps" (merge {:username (str (:_id (get-user username)))} others)))