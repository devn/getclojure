(ns getclojure.routes.home
  (:use [compojure.core]
        [hiccup.element])
  (:require [getclojure.views.layout :as layout]
            [getclojure.util :as util]))

(defn home-page [] 
  (layout/common
   (layout/search-form)))

(defn search-page [q num]
  (layout/common
   (layout/search-form)
   (layout/search-results q num)))

(defroutes home-routes 
  (GET "/" [] (home-page))
  (GET "/search" [q num]
       (println num)))