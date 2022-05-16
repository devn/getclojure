(ns getclojure.sexp
  (:refer-clojure :exclude [eval])
  (:require
   [clojure.string :as str]
   [getclojure.format :as fmt]
   [getclojure.util :as util]
   [schema.core :as s]
   [sci.core :as sci]
   [taoensso.timbre :as log])
  (:import
   (java.io StringWriter)
   (java.util.concurrent TimeUnit FutureTask TimeoutException)))

(def default-truncation-length 400)
(def default-timeout-millis 250)

(s/defn ^:private eval :- s/Any
  "Evaluate a string in SCI. Defined separately in case we want to supply
  additional options to sci/eval-string in the future. Note: We deny
  print-method and print-dup due to them modifying the hierarchy of the host and
  leaking across evaluations. alter-meta! is also disallowed as it spans
  evaluations.

  print-method, print-dup: https://github.com/babashka/sci/issues/726
  alter-meta!: https://github.com/babashka/sci/issues/733"
  [sexp-str :- s/Str]
  (sci/eval-string sexp-str {:deny ['print-method 'print-dup 'alter-meta!]}))

(s/defschema SexpResult
  {:input s/Str
   :output s/Str
   :value s/Str})

(s/defn ^:private run :- SexpResult
  "Runs a string like \"(inc 1)\" in SCI, and returns a map containing its :input,
  :output, and :value."
  [sexp-str :- s/Str
   truncation-length :- s/Num]
  (with-open [w (new StringWriter)]
    (sci/binding [sci/out w]
      (let [i sexp-str
            v (util/truncate truncation-length (pr-str (eval sexp-str)))
            o (util/truncate truncation-length (pr-str (str w)))]
        #_(log/info i v o)
        {:input i
         :value v
         :output o}))))

(s/defn ^:private thunk-timeout :- s/Any
  "Provided a `thunk` (fn [] ...) where ... is any expression, and `millis` is
  the number of milliseconds we'll wait before cancelling our task and the
  thread it runs in, returns the result of the expression inside the thunk
  unless it throws an Exception or does not complete within `millis`.

  Cancelling a future is insufficient. See amalloy's answer on StackOverflow
  here: https://stackoverflow.com/a/6697356"
  [thunk
   millis :- s/Num]
  (let [task (FutureTask. thunk)
        thread (Thread. task)]
    (try (.start thread)
         (.get task millis TimeUnit/MILLISECONDS)
         (catch TimeoutException _timeout-e
           (.cancel task true)
           (.stop thread))
         (catch Exception _e
           (.cancel task true)
           (.stop thread)))))

(s/defschema SexpColl
  (s/conditional sequential? [s/Str] set? #{s/Str}))

(s/defn ^:private run-coll :- [SexpResult]
  "Provided `timeout-millis` and a collection of s-expressions, runs each
  expression in SCI, returning only the ones that successfully run."
  [truncation-length :- s/Num
   timeout-millis :- s/Num
   sexp-coll :- SexpColl]
  (persistent!
   (reduce (fn [res s]
             (if-let [result (thunk-timeout (fn [] (run s truncation-length))
                                            timeout-millis)]
               (conj! res result)
               res))
           (transient [])
           sexp-coll)))

(s/defn remove-junk-sexps :- SexpColl
  "Provided a collection of strings, removes strings which are not interesting
  examples of expressions to search for."
  [coll :- SexpColl]
  (remove (fn [input]
            (or (str/starts-with? input "(doc")
                (str/starts-with? input "(source")
                ;; NOTE: There's a problem with pprint code-dispatch when
                ;; printing fn*. See:
                ;; http://dev.clojure.org/jira/browse/CLJ-1181
                (str/includes? input "fn*")))
          coll))

(s/defn remove-junk-values :- [SexpResult]
  "Provided a collection of `SexpResult` maps, removes expressions whose values
  we're not interested in."
  [sexp-coll :- [SexpResult]]
  (remove (fn [m]
            (str/starts-with? (:value m) "#object"))
          sexp-coll))

(s/defn filtered-run-coll :- [SexpResult]
  "Provided a collection of s-expression strings, removes expressions we're not
  interested in, runs them in SCI, and removes expressions which contain values
  we're not interested in."
  [sexp-coll :- SexpColl]
  (->> sexp-coll
       remove-junk-sexps
       (run-coll default-truncation-length default-timeout-millis)
       remove-junk-values))

(s/defschema FormattedSexpResult
  (merge SexpResult
         {:formatted-input s/Str
          :formatted-value s/Str
          :formatted-output (s/maybe s/Str)}))

(s/defn format-coll :- [FormattedSexpResult]
  "Provided a collection of s-expression maps, returns a map containing the
  formatted versions alongside the original input, output, and value. Logs a
  warning for expressions which fail formatting."
  [sexp-maps :- [SexpResult]]
  (mapv (fn [{:keys [input value output] :as m}]
          (try (merge m {:formatted-input (fmt/input input)
                         :formatted-value (fmt/value value)
                         :formatted-output (fmt/output output)})
               (catch Throwable _t
                 (log/warn {:input input
                            :value value
                            :output output}))))
        sexp-maps))

(comment
  (require '[criterium.core :as criterium])

  (criterium/quick-bench
   (inc 1))

  (require '[clj-async-profiler.core :as prof])
  (prof/profile {:width 2500 :min-width 5}
                (format-coll (filtered-run-coll ["(inc 1)"
                                                 "(doall (range))"])))

  (def server (prof/serve-files 8081))

  )

;; (sci/eval-string "(-> 1 inc inc)") ;; => 3
;; (sci/eval-string "(alter-meta! #'-> dissoc :macro)") ;; => nil
;; (sci/eval-string "(-> 1 inc inc)") ;; => #function[clojure.core/inc]
;; (sci/eval-string "(alter-meta! #'-> assoc :macro true)") ;; => nil
