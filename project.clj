(defproject getclojure "0.1.0"
  :description "GetClojure"
  :url "http://getclojure.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clojurewerkz/elastisch "1.0.2"]
                 [clojail "1.0.3"]
                 [com.novemberain/monger "1.5.0"]
                 [com.novemberain/validateur "1.4.0"]
                 [clj-config "0.2.0"]
                 [org.clojars.semperos/enlive "1.0.1" :exclusions [org.clojure/clojure]]
                 [me.raynes/conch "0.5.1"]
                 [org.thnetos/cd-client "0.3.6"]
                 ;; Web
                 [lib-noir "0.5.0"]
                 [com.cemerick/friend "0.1.5"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [ring-server "0.2.8"]
                 [com.taoensso/timbre "1.5.3"]
                 [markdown-clj "0.9.19"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler getclojure.handler/app
         :init getclojure.handler/init}
  :java-agents [[com.newrelic.agent.java/newrelic-agent "2.19.0"]]
  :profiles {:production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                          :offline true
                          :mirror {#"central|clojars"
                                   "http://s3pository.herokuapp.com/clojure"}}
             :dev {:dependencies [[ring-mock "0.1.3"]
                                  [ring/ring-devel "1.1.8"]
                                  [org.clojure/data.csv "0.1.2"]]}}
  :min-lein-version "2.0.0")
