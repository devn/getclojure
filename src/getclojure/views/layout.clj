(ns getclojure.views.layout
  (:use [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]
        [hiccup.form]
        [hiccup.page :only [html5 include-js include-css]]
        [getclojure.search :only (search-results-for)]
        [getclojure.util :only (generate-query-string)])
  (:require [monger.collection :as mc]))

(defn header []
  [:header
   [:h1 "GetClojure"]
   [:p "(find {:clojure :examples})"]])

(defn search-form [& q]
  (let [query (first q)]
    [:section.search
     (form-to
      [:get "/search"]
      (if query
        (text-field {:placeholder "comp AND juxt"} "q" query)
        (text-field {:placeholder "comp AND juxt"} "q"))
      (hidden-field "num" 0)
      (submit-button "search"))]))

(defn pagination [q]
  [:div#pagination
   (map #(link-to {:class "page_num"}
                  (str "/search?" (generate-query-string {"q" q "num" %})) %)
        (range 0 10))])

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
   [:div#pagination (map #(link-to {:class "page_num"}
                                   (str "/search?" (generate-query-string {"q" q "num" %})) %)
                         (range 0 10))]])

(defn footer []
  [:footer "Created with Love by '(Devin Walters)"])

(defhtml base [& content]
  [:head
   [:title "GetClojure"]
   (include-css "/css/screen.css" "/css/github.css")]
  [:body content
   (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js")])

(defn common [& content]
  (base
   (header)
   [:div#content content]
   (footer)))
