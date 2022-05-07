(ns getclojure.routes
  (:require
   [getclojure.views.layout :as layout]
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]))

(defn homepage
  []
  (layout/common
   (layout/search-form)))

(defn search-page
  [q num]
  (layout/common
   (layout/search-form q)
   (layout/search-results q num)))

(defroutes routes
  (GET "/" [] (homepage))
  (GET "/search" [q num] (search-page q num))
  (route/resources "/")
  (route/not-found "Route not found"))
