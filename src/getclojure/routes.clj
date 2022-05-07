(ns getclojure.routes
  (:require
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]
   [getclojure.views.layout :as layout]
   [schema.core :as s]))

(s/defn homepage :- s/Str
  []
  (layout/common
   (layout/search-form)))

(s/defn search-page :- s/Str
  [q :- s/Str
   num :- s/Str]
  (layout/common
   (layout/search-form q)
   (layout/search-results q num)))

(defroutes routes
  (GET "/" [] (homepage))
  (GET "/search" [q num] (search-page q num))
  (route/resources "/")
  (route/not-found "Route not found"))
