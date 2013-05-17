(ns getclojure.views.layout
  (:use [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to image]]
        [hiccup.form]
        [hiccup.page :only [html5 include-js include-css]]
        [getclojure.search :only (search-results-for get-num-hits)]
        [getclojure.util :only (generate-query-string)])
  (:require [monger.collection :as mc]))

(defn header []
  [:header
   (image {:class "getclojure-logo" :alt "Get Clojure"} "img/getclojure-logo.png")])

(defn search-form [& q]
  (let [query (first q)]
    [:section.search
     (form-to
      [:get "/search"]
      (if query
        (text-field {:placeholder "COMP AND JUXT"} "q" query)
        (text-field {:placeholder "COMP AND JUXT"} "q"))
      (hidden-field "num" 0)
      (submit-button {:id "search-box"} "search"))]))

(defn pagination [q page-num]
  (let [num (Integer/parseInt page-num)
        total-hits (get-num-hits q page-num)
        num-pages (/ total-hits 25)
        prev-page-num (dec num)
        next-page-num (inc num)]
    [:div#pagination
     (link-to {:class "prev"}
              (str "/search?" (generate-query-string {"q" q "num" prev-page-num}))
              "<<-")
     (map #(link-to {:class "page_num"}
                    (str "/search?" (generate-query-string {"q" q "num" %})) %)
          (range 0 num-pages))
     (link-to {:class "next"}
              (str "/search?" (generate-query-string {"q" q "num" next-page-num}))
              "->>")]))

(defn find-sexps-from-search [q page-num]
  (map #(mc/find-one-as-map "sexps" {:id %})
       (map :id (search-results-for q page-num))))

(defn search-results [q page-num]
  [:section.results
   [:ul
    (for [{:keys [input value output]} (find-sexps-from-search q page-num)]
      [:li.result
       input
       value
       output])]
   (pagination q page-num)])

(defn footer []
  [:footer
   "Created with "
   [:a {:href "https://github.com/devn/getclojure" :target "_blank"}
     [:span.love "Love"]]
   " by '(Devin Walters)"])

(defhtml base [& content]
  [:head
   [:title "GetClojure"]
   (include-css "/css/screen.css" "/css/github.css")
   [:script "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-41005509-1', 'getclojure.org');
  ga('send', 'pageview');"]]
  [:body content
   (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js")])

(defn common [& content]
  (base
   (header)
   [:div#content content]
   (footer)))
