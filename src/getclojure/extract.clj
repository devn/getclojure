(ns getclojure.extract
  (:require [net.cgrand.enlive-html :as enlive]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn get-lines [f]
  (enlive/select (enlive/html-resource f) [:p]))

(defn text-for [node kw]
  (first (enlive/texts (enlive/select node [kw]))))

(defn trim-nickname [s]
  (if s (str/replace s #": " "")))

(defn trim-content [s]
  (str/trimr (str/triml (str/trim-newline s))))

(defn extract-sexps
  "Extracts sexps."
  [string]
  (second
   (reduce (fn [[exp exps state cnt] c]
             (cond
              (= state :escape)
              [(.append exp c) exps :string cnt]
              (= state :string) (cond
                                 (= c \")
                                 [(.append exp c) exps :code cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 :else
                                 [(.append exp c) exps :string cnt])
              (and (= cnt 1) (= c \)))
              [(java.lang.StringBuilder.) (cons (str (.append exp c)) exps) :text 0]
              (= c \()
              [(.append exp c) exps :code (inc cnt)]
              (and (> cnt 1) (= c \)))
              [(.append exp c) exps :code (dec cnt)]
              (and (> cnt 0) (= c \"))
              [(.append exp c) exps :string cnt]
              (> cnt 0)
              [(.append exp c) exps :code cnt]
              :else [exp exps state cnt]))
           [(java.lang.StringBuilder.) '() :text 0]
           string)))

(defn node->map [node date]
  (let [nickname  (trim-nickname (text-for node :b))
        timestamp (text-for node :a)
        content   (trim-content (last (:content node)))
        sexps     (let [sexps (extract-sexps content)]
                    (if-not (empty? sexps) sexps))]
    {:nickname nickname
     :date date
     :timestamp timestamp
     :content content
     :sexp sexps}))

(defn fix-empty-nicknames [mapseq]
  (rest
   (reductions
    (fn [{prev :nickname} next]
      (update-in next [:nickname] #(or % prev)))
    {}
    mapseq)))

(defn log->mapseq [f]
  (let [date (str/replace (.getName f) #"\.html" "")
        log-lines (get-lines f)]
    (fix-empty-nicknames
     (map #(node->map % date) log-lines))))

(defn logs->mapseq [fcoll]
  (doseq [f fcoll]
    (println "Processing" (str f))
    (log->mapseq f)))
