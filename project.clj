(defproject getclojure "2.0.0"
  :description "GetClojure"
  :url "http://getclojure.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.babashka/sci "0.3.4"]
                 [cheshire "5.10.2"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [ring/ring-defaults "0.3.3"]
                 [compojure "1.6.2"]
                 [hiccup "1.0.5"]
                 [com.github.seancorfield/next.jdbc "1.2.780"]
                 [org.postgresql/postgresql "42.3.4"]
                 [com.outpace/config "0.13.5"]]
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
