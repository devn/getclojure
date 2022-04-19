(defproject getclojure "0.1.0"
  :description "GetClojure"
  :url "http://getclojure.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clojurewerkz/elastisch "3.0.1"]
                 [clojail "1.0.6"]
                 [com.novemberain/monger "3.5.0"]
                 [com.novemberain/validateur "2.6.0"]
                 [clj-config "0.2.0"]
                 [org.clojars.semperos/enlive "1.0.1" :exclusions [org.clojure/clojure]]
                 [me.raynes/conch "0.8.0"]
                 [org.thnetos/cd-client "0.3.6"]
                 ;; Web
                 [lib-noir "0.9.9"]
                 [com.cemerick/friend "0.2.3"]
                 [compojure "1.6.2"]
                 [hiccup "1.0.5"]
                 [ring-server "0.5.0"]
                 [com.taoensso/timbre "5.2.1"]
                 [markdown-clj "1.11.0"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler getclojure.handler/app
         :init getclojure.handler/init}
  :profiles {:production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                          :offline true
                          :mirror {#"central|clojars"
                                   "http://s3pository.herokuapp.com/clojure"}}
             :dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.9.5"]
                                  [org.clojure/data.csv "1.0.1"]]}}
  :min-lein-version "2.0.0")
