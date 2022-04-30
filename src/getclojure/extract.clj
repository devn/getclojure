(ns getclojure.extract
  (:require
   [clojure.string :as str]
   [net.cgrand.enlive-html :as enlive])
  (:import (java.io File)))

(defn get-lines
  "Gets all of the 'p' tags from a logfile."
  [^File logfile]
  (enlive/select (enlive/html-resource logfile) [:p]))

(defn text-for
  "Gets the text for an enlive node specified by its keyword."
  [node kw]
  (first (enlive/texts (enlive/select node [kw]))))

(defn trim-nickname
  "Takes \"foo: \" and returns \"foo\"."
  [^String s]
  (when s (str/replace s #": " "")))

(defn trim-content
  "Trim the left and right sides of a string of its whitespace and
  trailing newline.Takes and returns a string."
  [^String s]
  (str/trimr (str/triml (str/trim-newline s))))

(defn extract-sexps
  "Extracts sexps. Using 0s and 1s, ostensibly.

  Provided a string, hunts for s-expressions and returns them in a list. Returns
  empty list if none are found."
  [^String string]
  (second
   (reduce (fn [[exp exps state cnt] c]
             (cond
               (= state :escape)
               [(.append exp c) exps :string cnt]

               (= state :string) (case c
                                   \"
                                   [(.append exp c) exps :code cnt]

                                   \\
                                   [(.append exp c) exps :escape cnt]

                                   :else
                                   [(.append exp c) exps :string cnt])

               (and (= cnt 1) (= c \)))
               [(StringBuilder.) (cons (str (.append exp c)) exps) :text 0]

               (= c \()
               [(.append exp c) exps :code (inc cnt)]

               (and (> cnt 1) (= c \)))
               [(.append exp c) exps :code (dec cnt)]

               (and (> cnt 0) (= c \"))
               [(.append exp c) exps :string cnt]

               (> cnt 0)
               [(.append exp c) exps :code cnt]

               :else [exp exps state cnt]))

           [(StringBuilder.) '() :text 0]
           string)))

(defn extracted-sexps-or-nil
  "Takes a string and returns the s-expressions that were extracted a
  sequence of string, otherwise it returns nil."
  [^String s]
  (let [extracted-sexps (extract-sexps s)]
    (when (seq extracted-sexps) extracted-sexps)))

(defn node->map [node date]
  (let [nickname  (trim-nickname (text-for node :b))
        timestamp (text-for node :a)
        content   (trim-content (last (:content node)))
        sexps     (extracted-sexps-or-nil content)]
    {:nickname  nickname
     :date      date
     :timestamp timestamp
     :content   content
     :sexps     sexps}))

(defn forward-propagate
  "If the keyword (kw) specified does not exist in the next map in the
  sequence, use the previous value of the keyword (kw).

  Example:
  (forward-propagate :nickname '({:nickname \"Fred\"} {:nickname nil}))
  => ({:nickname \"Fred\"} {:nickname \"Fred\"})"
  [mapseq kw]
  (rest
   (reductions
    (fn [{prev kw} next]
      (update-in next [kw] #(or % prev)))
    {}
    mapseq)))

(defn logfile->mapseq
  "Takes a java.io.File and returns a sequence of hash maps which have the
  following keys: :nickname, :date, :timestamp, :content, :sexps."
  [^File logfile]
  (let [parsed-date  (str/replace (.getName logfile) #"\.html" "")
        loglines     (get-lines logfile)
        dated-mapseq (map #(node->map % parsed-date) loglines)]
    (forward-propagate :nickname dated-mapseq)))

(defn logfiles->mapseqs
  "Takes a sequence of java.io.File objects and returns a sequence of
  sequences containing maps which contains extracted information from
  each log line encountered.

  Example:
  (logs->mapseqs (map #(File. %) [\"pathto/file\" \"pathto/file2\"]))
  => ({:input \"(+ 1 1)\" ...} {:input \"(+ 1 2)\"})"
  [logfiles]
  (doseq [logfile logfiles] (logfile->mapseq logfiles)))
