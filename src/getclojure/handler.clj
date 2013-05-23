(ns getclojure.handler
  (:require [compojure.route :as route]
            [monger.core :as esr]
            [noir.util.middleware :as middleware]
            [compojure.core :refer [defroutes]]
            [getclojure.db :refer [make-connection!]]
            [getclojure.models.sexp :refer [set-highest-sexp-id!]]
            [getclojure.routes.home :refer [home-routes]]
            [taoensso.timbre :refer [info]]))

(defroutes app-routes
  (route/resources "/")
  (route/resources "/search")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when app is deployed as a servlet on an
   app server such as Tomcat put any initialization code here"
  []
  (info "DB:" (make-connection!))
  (set-highest-sexp-id!)
  (info "SEARCH:" (esr/connect! (or (System/getenv "BONSAI_URL")
                                    "http://127.0.0.1:9200")))
  (println "GetClojure started successfully..."))

(defn destroy []
  (println "Shutting down..."))

;;append your application routes to the all-routes vector
(def all-routes [home-routes app-routes])
(def app (middleware/app-handler all-routes))
(def war-handler (middleware/war-handler app))
