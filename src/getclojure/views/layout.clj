(ns getclojure.views.layout
  (:require
   [getclojure.util :refer [generate-query-string]]
   [hiccup.def :refer [defhtml]]
   [hiccup.element :refer [image link-to]]
   [hiccup.form :refer [form-to
                        hidden-field
                        submit-button
                        text-field]]
   [hiccup.page :refer [include-css include-js]]
   [clojure.java.io :as io]
   [cheshire.core :as json]))

(defn header []
  [:header
   [:a {:href "/" :rel "home"}
     (image {:class "getclojure-logo" :alt "Get Clojure"} "img/getclojure-logo.png")]])

(defn search-form [& q]
  (let [query (first q)]
    [:section.search
     (form-to
      [:get "/search"]
      (if query
        (text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                     :spellcheck "false" :placeholder "iterate AND range"} "q" query)
        (text-field {:autocorrect "off" :autocapitalize "off" :autocomplete "off"
                     :spellcheck "false" :placeholder "iterate AND range"} "q"))
      (hidden-field "num" 0)
      (submit-button {:id "search-box"} "search"))]))

#_(defn pagination [q page-num]
  (let [num (Integer/parseInt page-num)
        total-hits (get-num-hits q page-num)
        num-pages (/ total-hits 25)
        prev-page-num (dec num)
        next-page-num (inc num)]
    (when-not (< num-pages 1)
      [:div#pagination
       (when-not (zero? num)
         [:div.prev-links
          (link-to {:class "first-page"}
                   (str "/search?" (generate-query-string {"q" q "num" 0}))
                   "<<- ")
          (link-to {:class "prev"}
                   (str "/search?" (generate-query-string {"q" q "num" prev-page-num}))
                   "<- ")])
       (let [page-links (map (fn [p-num]
                               (link-to {:class "page_num"}
                                        (str "/search?"
                                             (generate-query-string
                                              {"q" q "num" p-num}))
                                        (inc p-num)))
                             (range num num-pages))
             num-page-links (count page-links)]
         (if (< num-page-links 10)
           page-links
           (take 10 page-links)))
       (when (<= next-page-num num-pages)
         [:div.next-links
          (link-to {:class "next"}
                   (str "/search?" (generate-query-string {"q" q "num" next-page-num}))
                   " ->")
          (link-to {:class "last-page"}
                   (str "/search?"
                        (generate-query-string {"q" q "num" (-> (Math/floor num-pages)
                                                                int
                                                                str)}))
                   " ->>")])])))

#_(defn find-sexps-from-search [q page-num]
  (map #(mc/find-one-as-map @db/db "sexps" {:id %})
       (map :id (search-results-for q page-num))))

(def temp-sexps (json/decode (slurp "output.json") true))

(defn search-results [q page-num]
  [:section.results
   [:ul
    (for [{:keys [formatted-input
                  formatted-value
                  formatted-output]} (take 25 (shuffle temp-sexps))]
      [:li.result
       formatted-input
       formatted-value
       formatted-output])]
   #_(pagination q page-num)])

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

(defhtml on-github []
  [:div.on-github
   [:a {:href "https://github.com/devn/getclojure"}
    [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
           :src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
           :alt "Fork me on GitHub"}]]])

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
  [:body
   (on-github)
   content
   (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js")])

(defn common [& content]
  (base
   (header)
   [:div#content content]
   (footer)))
