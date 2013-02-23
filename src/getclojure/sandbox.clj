(ns getclojure.sandbox)

(def runnable-sexps (atom []))

(defn extract-sexps-from-logfile [logfile]
  (let [lines-with-sexps]
    (println "Extracting log lines from" (str logfile))
    (doseq [line ])))

(def working-sexps (atom (read-string (slurp "working-sexps.db"))))
