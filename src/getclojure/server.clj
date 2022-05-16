(ns getclojure.server
  (:require
   [getclojure.routes :as routes]
   [ring.adapter.jetty :as ring]
   [ring.middleware.defaults :as ring.defaults]
   [taoensso.timbre :as log]))

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error ex "Uncaught exception on" (.getName thread)))))

(def app (ring.defaults/wrap-defaults
          #'routes/routes
          ring.defaults/site-defaults))

(defn -main []
  (ring/run-jetty #'app {:port 8080
                         :join? false}))
