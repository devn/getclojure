(ns getclojure.models.user
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as s]
            [monger.collection :as mc]
            [noir.session :as session]
            [getclojure.config :refer [config]]
            [getclojure.db :as db]
            [getclojure.util :refer [inclusive-range]]
            [monger.query :refer [sort find paginate with-collection]]
            [validateur.validation :refer [format-of
                                           length-of
                                           validation-set]])
  (:import (org.bson.types ObjectId))
  (:refer-clojure :exclude [sort find]))

;; TODO: It would be nice to have uniqueness-of in here
(def validate-user-map
  (validation-set
   (length-of :username :within (inclusive-range 3 16))
   (format-of :username :format #"^[a-zA-Z0-9_]*$")))

(defn unique-user? [username]
  (nil? (mc/find-one-as-map @db/db "users" {:username username})))

(defn create-user! [email username]
  (let [name (s/lower-case username)
        qmap {:email email
              :username name}
        errors (validate-user-map qmap)]
    (if (and (unique-user? name) (empty? errors))
      (let [user (mc/insert-and-return @db/db "users" qmap)]
        ;; TODO: Finish building out users
        ;;(session/put! :user (assoc qmap :id (str (:_id user))))
        user)
      errors)))

(defn user-exists [email]
  (when-let [{:keys [username _id]} (mc/find-one-as-map @db/db "users" {:email email})]
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
  (mc/find-one-as-map @db/db "users" {:username username}))

(defn get-user-by-id [id]
  (mc/find-map-by-id @db/db "users" (ObjectId. id)))

(defn user-sexps [username page & [others]]
  (with-collection @db/db "sexps"
    (find (merge {:user (:_id (get-user username))} others))
    (sort {:date -1})
    (paginate :page page :per-page 10)))

(defn count-user-sexps [username & [others]]
  (mc/count @db/db "sexps" (merge {:user (:_id (get-user username))} others)))
