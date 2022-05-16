(ns getclojure.views.layout
  (:require
   [getclojure.elastic :as elastic]
   [getclojure.util :as util]
   [hiccup
    [def :as h.def]
    [element :as h.element]
    [form :as h.form]
    [page :as h.page]]))

(defn header
  "Header!"
  []
  [:header
   [:a {:href "/" :rel "home"}
    (h.element/image {:class "getclojure-logo"
                      :alt "Get Clojure"}
                     "img/getclojure-logo.png")]])

(defn search-form
  "Optionally provided a query, spits out the search box section of the page."
  [& q]
  (let [text-field-opts {:autocorrect "off"
                         :autocapitalize "off"
                         :autocomplete "off"
                         :spellcheck "false"
                         :autofocus "false"
                         :placeholder "iterate AND inc"}]
    [:section.search
     (h.form/form-to
      [:get "/search"]
      (if (seq q)
        (h.form/text-field (assoc text-field-opts
                                  :value (first q))
                           "q")
        (h.form/text-field (assoc text-field-opts :autofocus "true") "q"))
      (h.form/hidden-field "num" 0)
      [:input {:type "submit"
               :id "search-box"
               :value "search"}])]))

(defn pagination
  "Provided a query string, a page number, and a total number of pages, returns
  the HTML for pagination links on the page."
  [q page-num total-pages]
  (let [page-num (Long/parseLong page-num)
        prev-page-num (dec page-num)
        next-page-num (inc page-num)]
    (when-not (= total-pages 1)
      [:div#pagination
       (when-not (zero? total-pages)
         [:div.prev-links
          (h.element/link-to {:class "first-page"}
                             (str "/search?" (util/generate-query-string {"q" q
                                                                          "num" 0}))
                             "<<- ")
          (h.element/link-to {:class "prev"}
                             (str "/search?" (util/generate-query-string {"q" q
                                                                          "num" (if (< prev-page-num 0)
                                                                                  0
                                                                                  prev-page-num)}))
                             "<- ")])


       (let [page-links (map (fn [p-num]
                               (h.element/link-to {:class "page_num"}
                                                  (str "/search?"
                                                       (util/generate-query-string
                                                        {"q" q
                                                         "num" p-num}))
                                                  (inc p-num)))
                             (range page-num total-pages))
             num-page-links (count page-links)]
         (if (< num-page-links 10)
           page-links
           (take 10 page-links)))


       (when (<= next-page-num (dec total-pages))
         [:div.next-links
          (h.element/link-to {:class "next"}
                             (str "/search?" (util/generate-query-string {"q" q
                                                                          "num" next-page-num}))
                             " ->")
          (h.element/link-to {:class "last-page"}
                             (str "/search?"
                                  (util/generate-query-string {"q" q
                                                               "num" (dec total-pages)}))
                             " ->>")])])))

(defn search-results
  "Provided a query and a page number, generates the HTML for each s-expression
  result."
  [q page-num]
  (let [{:keys [hits total-pages]} (elastic/search elastic/conn q (Long/parseLong page-num))]
    [:section.results
     [:ul
      (for [{:keys [formatted-input
                    formatted-value
                    formatted-output]} hits]
        [:li.result
         formatted-input
         formatted-value
         formatted-output])]
     (pagination q page-num total-pages)]))

(h.def/defhtml powered-by
  []
  [:a.powered-by-clojure {:href "http://clojure.org/"
                          :title "Clojure"}
   (h.element/image {:height 32
                     :width 32
                     :alt "Powered by Clojure"}
                    "img/clojure-icon.gif")
   "Powered by Clojure"])

(h.def/defhtml created-by
  []
  [:div.created-by
   (h.element/image {:class "heart" :alt "heart"} "img/heart.png")
   "Created with Love by '(Devin Walters)"])

(h.def/defhtml on-github
  []
  [:div.on-github
   [:a {:href "https://github.com/devn/getclojure"}
    [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
           :src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
           :alt "Fork me on GitHub"}]]])

(h.def/defhtml footer
  []
  [:footer
   (created-by)
   (powered-by)])

(h.def/defhtml base
  [& content]
  [:head
   [:title "GetClojure"]
   (h.page/include-css "/css/screen.css" "/css/github.css")
   [:script "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-41005509-1', 'getclojure.org');
  ga('send', 'pageview');"]]
  [:body
   (on-github)
   content])

(defn common
  "The common wrapper around every page's content."
  [& content]
  (base
   (header)
   [:div#content content]
   (footer)))
