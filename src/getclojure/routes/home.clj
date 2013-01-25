(ns getclojure.routes.home
  (:use [compojure.core]
        [hiccup.element])
  (:require [getclojure.views.layout :as layout]
            [getclojure.util :as util]))

(defn home-page [] 
  (layout/common
   (layout/search-form)))

(defn search-page [q]
  (layout/common
   (layout/search-form)
   (layout/search-results q)))

(defroutes home-routes 
  (GET "/" [] (home-page))
  (GET "/search" [q] (search-page q)))