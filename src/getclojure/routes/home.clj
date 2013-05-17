(ns getclojure.routes.home
  (:require [getclojure.views.layout :as layout]
            [compojure.core :refer [GET defroutes]]))

(defn home-page [] 
  (layout/common
   (layout/search-form)))

(defn search-page [q num]
  (layout/common
   (layout/search-form q)
   (layout/search-results q num)))

(defroutes home-routes 
  (GET "/" [] (home-page))
  (GET "/search" [q num]
       (search-page q num)))