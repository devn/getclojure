(ns getclojure.views.layout
  (:require [monger.collection :as mc]
            [getclojure.search :refer [get-num-hits search-results-for]]
            [getclojure.util :refer [generate-query-string]]
            [hiccup.def :refer [defhtml]]
            [hiccup.element :refer [image link-to]]
            [hiccup.form :refer [form-to
                                 hidden-field
                                 submit-button
                                 text-field]]
            [hiccup.page :refer [include-css include-js]]))

(defn header []
  [:header
   (image {:class "getclojure-logo" :alt "Get Clojure"} "img/getclojure-logo.png")])

(defn search-form [& q]
  (let [query (first q)]
    [:section.search
     (form-to
      [:get "/search"]
      (if query
        (text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                     :spellcheck "false" :placeholder "COMP AND JUXT"} "q" query)
        (text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                     :spellcheck "false" :placeholder "COMP AND JUXT"} "q"))
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

(defhtml powered-by []
  [:a.powered-by-clojure {:href "http://clojure.org/"
                          :title "Clojure"}
   (image {:height 32 :width 32}
          "img/clojure-icon.gif"
          "Powered by Clojure")
   "Powered by Clojure"])

(defhtml created-by []
  [:div.created-by
   (image {:class "heart"} "img/heart.png" "heart")
   "Created with Love by '(Devin Walters)"])

(defhtml footer []
  [:footer (created-by) (powered-by)])

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
