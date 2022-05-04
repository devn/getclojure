(ns getclojure.extract
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [net.cgrand.enlive-html :as enlive]
   [schema.core :as s])
  (:import (java.io File)))

(s/defn local-logs :- [File]
  "Returns a collection of HTML files in the logs directory."
  []
  (filter #(re-find #"\.*\.html" (str %))
          (file-seq (io/as-file (io/resource "logs")))))

(s/defn get-lines :- s/Any
  "Gets all of the 'p' tags from a logfile."
  [logfile :- File]
  (enlive/select (enlive/html-resource logfile) [:p]))

(s/defn text-for :- s/Any
  "Gets the text for an enlive node specified by its keyword."
  [node :- s/Any
   kw :- s/Keyword]
  (first (enlive/texts (enlive/select node [kw]))))

(s/defn trim-nickname :- (s/maybe s/Str)
  "Takes \"foo: \" and returns \"foo\"."
  [s :- (s/maybe s/Str)]
  (when s (str/replace s #": " "")))

(s/defn trim-content :- s/Str
  "Trim the left and right sides of a string of its whitespace and
  trailing newline.Takes and returns a string."
  [s :- s/Str]
  (str/trimr (str/triml (str/trim-newline s))))

(s/defn extract-sexps :- (s/maybe [s/Str])
  "Extracts sexps. Using 0s and 1s, ostensibly.

  Provided a string, hunts for s-expressions and returns them in a list. Returns
  empty list if none are found."
  [string :- (s/maybe s/Str)]
  (second
   (reduce (fn [[exp exps state cnt] c]
             (cond
               (= state :escape)
               [(.append exp c) exps :string cnt]

               (= state :string) (case c
                                   \" [(.append exp c) exps :code cnt]

                                   \\ [(.append exp c) exps :escape cnt]

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

(s/defn extracted-sexps-or-nil :- (s/maybe [s/Str])
  "Takes a string `s` and returns the s-expressions that were extracted as a
  sequence of strings, otherwise returns nil."
  [s :- s/Str]
  (when-let [extracted-sexps (seq (extract-sexps s))]
    extracted-sexps))

(s/def Node {(s/required-key :tag) (s/eq :p)
             (s/required-key :attrs) (s/maybe {s/Keyword s/Any})
             (s/required-key :content) [(s/one {(s/required-key :tag) (s/eq :a)
                                                (s/required-key :attrs) {(s/required-key :name) s/Str
                                                                         (s/optional-key :class) s/Str}
                                                (s/required-key :content) [s/Str]}
                                               "timestamp")
                                        (s/one s/Str "empty-or-message")
                                        (s/optional {(s/required-key :tag) (s/eq :b)
                                                     (s/required-key :attrs) (s/maybe {s/Keyword s/Any})
                                                     (s/required-key :content) [s/Str]}
                                                    "nickname")
                                        (s/optional s/Str "message")]})

(s/def NodeMap {:nickname (s/maybe s/Str)
                :date s/Str
                :timestamp s/Str
                :content s/Str
                :sexps [(s/maybe s/Str)]})

(s/defn node->map :- NodeMap
  "Provided a `node` and a `date` as a string, returns a map containing
  `:nickname`, `:date`, `:timestamp`, `:content`, and `:sexps`."
  [node :- Node
   date :- s/Str]
  (let [nickname  (trim-nickname (text-for node :b))
        timestamp (text-for node :a)
        content   (trim-content (last (:content node)))
        sexps     (extracted-sexps-or-nil content)]
    {:nickname  nickname
     :date      date
     :timestamp timestamp
     :content   content
     :sexps     sexps}))

(s/defn forward-propagate :- [{s/Keyword s/Any}]
  "If the keyword (`kw`) specified does not exist in the next map in the sequence,
  use the previous value of the keyword (`kw`).

  Example:
  (forward-propagate :nickname '({:nickname \"Fred\"} {:nickname nil}))
  => ({:nickname \"Fred\"} {:nickname \"Fred\"})"
  [mapseq :- [{s/Keyword s/Any}]
   kw :- s/Keyword]
  (rest
   (reductions
    (fn [{prev kw} next]
      (update-in next [kw] #(or % prev)))
    {}
    mapseq)))

(s/def Entry {:nickname  s/Str
              :date      s/Str
              :timestamp s/Str
              :content   s/Str
              :sexps     [(s/maybe s/Str)]})

(s/def Entries [Entry])

(s/defn logfile->mapseq :- Entries
  "Takes a java.io.File and returns a sequence of hash maps which have the
  following keys: `:nickname`, `:date`, `:timestamp`, `:content`, `:sexps`."
  [logfile :- File]
  (let [parsed-date  (str/replace (.getName logfile) #"\.html" "")
        loglines     (get-lines logfile)
        dated-mapseq (map #(node->map % parsed-date) loglines)]
    (forward-propagate dated-mapseq :nickname)))


(defn logfiles->mapseqs
  "Takes a sequence of java.io.File objects and returns a sequence of
  sequences containing maps which contains extracted information from
  each log line encountered.

  Example:
  (logfiles->mapseqs (map #(File. %) [\"pathto/file\" \"pathto/file2\"]))
  => ({:input \"(+ 1 1)\" ...} {:input \"(+ 1 2)\" ...} ...)"
  [logfiles f]
  (doseq [logfile logfiles]
    (f (logfile->mapseq logfile))))

(comment

  (logfiles->mapseqs (local-logs)
                     (fn [maps]
                       (doseq [m maps]
                         #_(take the Entry and put it in a database))))

  )
