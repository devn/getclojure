(ns getclojure.queue)

(defmethod print-method clojure.lang.PersistentQueue
  [q, w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(defn make-queue [] clojure.lang.PersistentQueue/EMPTY)
