(ns getclojure.core
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [compojure.core :refer [defroutes GET]]
   [ring.adapter.jetty :as ring]
   [ring.middleware.defaults :as ring.defaults]
   [hiccup.core :as h]
   [hiccup.page :as h.page]
   [hiccup.element :as h.element]
   [hiccup.form :as h.form]
   [hiccup.def :as h.def :refer (defhtml)]
   [compojure.route :as route]))


(def sexps (json/decode (slurp "output.json") true))

(defn header []
  [:header
   [:a {:href "/"
        :rel "home"}
    (h.element/image {:class "getclojure-logo"
                      :alt "Get Clojure"}
                     "img/getclojure-logo.png")]])

(defn search-form [& q]
  (let [query (first q)]
    [:section.search
     (h.form/form-to
      [:get "/search"]
      (if query
        (h.form/text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                            :spellcheck "false" :placeholder "iterate AND range"} "q" query)
        (h.form/text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                            :spellcheck "false" :placeholder "iterate AND range"} "q"))
      (h.form/hidden-field "num" 0)
      (h.form/submit-button {:id "search-box"}
                            "search"))]))

(defn base [& content]
  )

(defn common [& content]
  )

(defn layout [])
(defn home
  []
  (h.page/html5
   [:head
    (h.page/include-css "/css/screen.css"
                        "/css/github.css")
    [:title "Hello"]]
   [:body
    (header)
    (search-form)
    [:section.results
     [:ul
      (for [{:keys [formatted-input
                    formatted-value
                    formatted-output]} (take 25 (shuffle sexps))]
        [:li.result
         formatted-input
         formatted-value
         formatted-output])]]]))

(defroutes routes
  (GET "/" [] (home))
  (GET "/search" [q num])
  (route/resources "/")
  (route/not-found "boom"))

(def app (ring.defaults/wrap-defaults routes ring.defaults/site-defaults))

(defn -main []
  (ring/run-jetty #'app {:port 8080
                         :join? false}))

(comment

  (def server (ring/run-jetty #'app {:port 8080
                                     :join? false}))

  )
