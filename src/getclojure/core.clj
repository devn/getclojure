(ns getclojure.core
  (:require
   [getclojure.routes :as routes]
   [ring.adapter.jetty :as ring]
   [ring.middleware.defaults :as ring.defaults]))

(def app (ring.defaults/wrap-defaults
          #'routes/routes
          ring.defaults/site-defaults))

(defn -main []
  (ring/run-jetty #'app {:port 8080
                         :join? false}))

(comment

  (def server (ring/run-jetty #'app {:port 8080
                                     :join? false}))
  (.stop server)

  )
