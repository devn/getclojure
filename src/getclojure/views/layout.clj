(ns getclojure.views.layout
  (:use hiccup.form
        [hiccup.def :only [defhtml]]
        [hiccup.element :only [link-to]]
        [hiccup.form]
        [hiccup.page :only [html5 include-js include-css]]
        [getclojure.search :only (search-results-for)]
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
    (submit-button "search"))])

(defn search-results [q]
  [:section.results
   [:ul
    (for [{:keys [input value output]} (search-results-for q)]
      [:li.result
       (if (string? input)
         [:pre.input (str input "\n")]
         [:pre.input (with-out-str
                       (pp/with-pprint-dispatch pp/code-dispatch
                         (pp/pprint (safe-read input))))])
       [:pre.value value]
       (if-not (= output "\"\"") [:pre.output output])])]])

(defn footer []
  [:footer "Created with Love by '(Devin Walters)"])

(defhtml base [& content]
  [:head
   [:title "GetClojure"]
   (include-css "/css/screen.css")]
  [:body content])

(defn common [& content]
  (base
   (header)
   [:div#content content]
   (footer)))