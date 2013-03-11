(ns getclojure.handler
  (:use [getclojure.routes.home]
        [compojure.core])
  (:require [noir.util.middleware :as middleware]
            [clojurewerkz.elastisch.rest :as esr]
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
  (let [uri (or (System/getenv "MONGODB_URI")
              "mongodb://127.0.0.1/getclojure_development")
        hard-coded-uri "mongodb://heroku_app11300183:5i1uhb3oojqo6da8qe829f58c0@ds029297.mongolab.com:29297/heroku_app11300183"]
    (println "Connecting to Mongo URI:" hard-coded-uri)
    (mg/connect-via-uri! hard-coded-uri))
  (esr/connect! (or (System/getenv "BONSAI_URL") "http://127.0.0.1:9200"))
  (println "GetClojure started successfully..."))

(defn destroy []
  (println "Shutting down..."))

;;append your application routes to the all-routes vector
(def all-routes [home-routes app-routes])
(def app (middleware/app-handler all-routes))
(def war-handler (middleware/war-handler app))
