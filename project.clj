(defproject getclojure "0.1.0"
  :description "GetClojure"
  :url "http://getclojure.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clojurewerkz/elastisch "1.0.2"]
                 [clojail "1.0.3"]
                 [org.clojars.semperos/enlive "1.0.1"
                  :exclusions [org.clojure/clojure]]
                 [com.cemerick/pomegranate "0.0.13"
                  :exclusions [org.apache.httpcomponents/httpcore]]]
  :jvm-options ["-Xmx4g" "-server"]
  :jvm-opts ["-Xmx4g" "-server"])
