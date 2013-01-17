(ns getclojure.routes.home
  (:use compojure.core hiccup.element)
  (:require [getclojure.views.layout :as layout]
            [getclojure.util :as util]))

(defn home-page [] 
  (layout/common
    (util/md->html "/md/docs.md")))

(defn about-page []
  (layout/common
   "this is the story of getclojure... work in progress"))

(defroutes home-routes 
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))