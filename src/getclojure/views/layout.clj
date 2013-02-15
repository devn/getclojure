(ns getclojure.views.layout
  (:use hiccup.form
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]
        [hiccup.form]
        [hiccup.page :only [html5 include-js include-css]]
        [getclojure.search :only (search-results-for)]
        [getclojure.views.helpers :only (format-input format-output format-value)]
        [clojail.core :only (safe-read)]
        [clojure.pprint :as pp]))

(defn header []
  [:header
   [:h1 "GetClojure"]
   [:p "(find {:clojure :examples})"]])

(defn search-form []
  [:section.search
   (form-to
    [:get "/search"]
    (text-field {:placeholder "comp AND juxt"} "q")
    (hidden-field "num" 0)
    (submit-button "search"))])

(defn search-results [q page-num]
  [:section.results
   [:ul
    (for [{:keys [input value output]} (search-results-for q page-num)]
      [:li.result
       (format-input input)
       (format-value value)
       (format-output output)])]
   [:div#pagination (map #(link-to (str "/search?num=" %) %) (range 0 10))]])

(defn footer []
  [:footer "Created with Love by '(Devin Walters)"])

(defhtml base [& content]
  [:head
   [:title "GetClojure"]
   (include-css "/css/screen.css")
   (include-css "/css/prettify.css")
   (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js")
   (include-js "/js/prettify.js")
   (include-js "/js/lang-clj.js")
   (include-js "/js/main.js")]
  [:body content])

(defn common [& content]
  (base
   (header)
   [:div#content content]
   (footer)))