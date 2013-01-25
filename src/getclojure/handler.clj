(ns getclojure.handler
  (:use [getclojure.routes.home]
        [compojure.core]
        [getclojure.search :only (create-getclojure-index add-to-index)])
  (:require [noir.util.middleware :as middleware]
            [clojurewerkz.elastisch.rest :as esr]
            [compojure.route :as route]
            [clojure.java.io :as io]))

(def sexps
  (into #{} (read-string (slurp (io/file "working-sexps.db")))))

(defn add-sexps-to-index []
  (doseq [sexp sexps]
    (add-to-index :getclojure_development sexp)))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when app is deployed as a servlet on an
   app server such as Tomcat put any initialization code here"
  []
  (esr/connect! (or (System/getenv "SEARCHBOX_URL")
                    "http://127.0.0.1:9200"))
  (create-getclojure-index)
  (time (add-sexps-to-index))
  (println "GetClojure started successfully..."))

(defn destroy []
  (println "Shutting down..."))

;;append your application routes to the all-routes vector
(def all-routes [home-routes app-routes])
(def app (middleware/app-handler all-routes))
(def war-handler (middleware/war-handler app))
