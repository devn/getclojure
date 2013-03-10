(ns getclojure.handler
  (:use [getclojure.routes.home]
        [compojure.core])
  (:require [noir.util.middleware :as middleware]
            [clojurewerkz.elastisch.rest :as esr]
            [getclojure.db :refer [make-connection!]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [compojure.route :as route]))

(defroutes app-routes
  (route/resources "/")
  (route/resources "/search")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when app is deployed as a servlet on an
   app server such as Tomcat put any initialization code here"
  []
  (make-connection!)
  (esr/connect! (or (System/getenv "BONSAI_URL") "http://127.0.0.1:9200"))
  (println "GetClojure started successfully..."))

(defn destroy []
  (println "Shutting down..."))

;;append your application routes to the all-routes vector
(def all-routes [home-routes app-routes])
(def app (middleware/app-handler all-routes))
(def war-handler (middleware/war-handler app))
