;; functional programming languages encourage the use of immutable
;; objects - referred to as values - to represent program state.

;; the more you can make your program rely upon operations over
;; values, the easier it will be to reason about the program's
;; behavior.

;; A key characteristic of values are the semantics they ensure with
;; regard to equality and comparison, at a single point in time as
;; well as over time.

;; Clojure data structures are immutable and efficient
(def h {[1 2] 3})
(h [1 2])
;= 3
(conj (first (keys h)) 3)
;= [1 2 3]
(h [1 2])
;= 3
h
;= {[1 2] 3}

;; functions as data permits a means of abstraction that a language
;; without first-class functions lacks.

(defn call-twice [f x]
  (f x)
  (f x))

(call-twice println 123)

(max 5 6)
;= 6

(require 'clojure.string)
(clojure.string/lower-case "Clojure")
;= "clojure"

;; Clojure functions are values themselves, they can be used with
;; higher-order fuctions, which are functions that take other
;; functions as arguments or return a function as a result.

(map clojure.string/lower-case ["Java" "Imperative" "Weeping"
                                "Clojure" "Learning" "Peace"])
;= ("java" "imperative" "weeping" "clojure" "learning" "peace")
(map * [1 2 3 4] [5 6 7 8])
;= (5 12 21 32)

(reduce max [0 -3 10 48])
;= 48

;; provide an initial value to "seed" the reduction
(reduce + 50 [1 2 3 4])
;= 60

(reduce
 (fn [m v]
   (assoc m v (* v v)))
 {}
 [1 2 3 4])
;= {4 16, 3 9, 2 4, 1 1}

(reduce
 #(assoc %1 %2 (* %2 %2))
 {}
 [1 2 3 4])
;= {4 16, 3 9, 2 4, 1 1}

;; Applying ourselves partially [ page 65 ]

;; function application is available in Clojure via apply
(apply hash-map [:a 5 :b 6])
;= {:a 5, :b 6}

;; apply allows you to prefix the argument sequence with any number of
;; explicit arguments.
(def args [2 -2 10])
(apply * 0.5 3 args)
;= -60.0

;; apply must be provided with all arguments to the function applied.
;; partial application is where you can provide only some of the
;; arguments to a function yielding a new function that can be called
;; with the reminder of the arguments to the original function later.

;; partial provides for partial application in Clojure
(def only-strings (partial filter string?))
;= #'user/only-strings
(only-strings ["a" 5 "b" 6])
;= ("a" "b")

;; partial versus function literals

;; function literals technically provide a superset of what partial
;; provides.
(#(filter string? %) ["a" 5 "b" 6])
;= ("a" "b")

;; function literals do not limit you to defining only the initial
;; arguments to the function
(#(filter % ["a" 5 "b" 6]) string?)
;= ("a" "b")
(#(filter % ["a" 5 "b" 6]) number?)
;= (5 6)

;; But function literals force you to fully specify all the arguments
;; to the function, whereas partial allows you to be ignorant of such
;; details
(#(map *) [1 2 3] [4 5 6] [7 8 9])
;= clojure.lang.ArityException: Wrong number of args (3) passed to:
(#(map * %1 %2 %3) [1 2 3] [4 5 6] [7 8 9])
;= (28 80 162)

(#(apply map * %&) [1 2 3] [4 5 6] [7 8 9])
;= (28 80 162)
(#(apply map * %&) [1 2 3])
;= (1 2 3)

((partial map *) [1 2 3] [4 5 6] [7 8 9])
;= (28 80 162)

;; compositionality of functions refer to the ability of various parts
;; to be joined together to create a well-formed composite that is
;; itself reusable.

(defn negated-sum-str
  [& numbers]
  (str (- (apply + numbers))))
(negated-sum-str 10 12 3.4)
;= "-25.4"

;; function composition, implemented in Clojure via comp
;; you can think of comp as defining a pipeline
(def negated-sum-str (comp str - +))
(negated-sum-str 10 12 3.4)
;= "-25.4"

(require '[clojure.string :as str])

(def camel->keyword (comp keyword
                          str/join
                          (partial interpose \-)
                          (partial map str/lower-case)
                          #(str/split % #"(?<=[a-z])(?=[A-Z])")))
;= #'user/camel->keyword
(camel->keyword "CamelCase")
;= :camel-case
(camel->keyword "lowerCamelCase")
;= :lower-camel-case

(def camel-pairs-map (comp (partial apply hash-map)
                           (partial map-indexed (fn [i x]
                                                  (if (odd? i)
                                                    x
                                                    (camel->keyword x))))))
(camel-pairs-map ["CamelCase" 5 "lowerCamelCase" 6])
;= {:camel-case 5, :lower-camel-case 6}

;; You can achieve much the same effect as comp using the -> and ->>
;; macros.
(defn camel->keyword
  [s]
  (->> (str/split s #"(?<=[a-z])(?=[A-Z])")
       (map str/lower-case)
       (interpose \-)
       str/join
       keyword))

;; a HOF that returns a function that adds a given number to its
;; argument
(defn adder [n]
  (fn [x] (+ n x)))
;= #'user/adder
((adder 5) 18)
;= 23

;; a higher-order function that doubles the result of calling the
;; provided function.
(defn doubler [f]
  (fn [& args]
    (* 2 (apply f args))))
;= #'user/doubler
(def double-+ (doubler +))
;= #'user/double-+
(double-+ 1 2 3)
;= 12

;; building a primitive logging system with composable higher-order
;; functions [ page 72 ]

;; a higher-order function that returns a function that prints
;; messages to any writer provided
(defn print-logger [writer]
  #(binding [*out* writer]
     (println %)))

(def *out*-logger (print-logger *out*))
;= #'user/*out*-logger
(*out*-logger "hello")
; hello
;= nil

(def writer (java.io.StringWriter.))
;= #'user/writer
(def retained-logger (print-logger writer))
;= #'user/retained-logger
(retained-logger "hello")
;= nil
(str writer)
;= "hello\n"

(require 'clojure.java.io)

(defn file-logger [file]
  #(with-open [f (clojure.java.io/writer file :append true)]
     ((print-logger f) %)))
;= #'user/file-logger

(def log->file (file-logger "messages.log"))
;= #'user/log->file
(log->file "hello")
;= nil

;; a higher-order function that routes a message to multiple loggers
(defn multi-logger [& logger-fns]
  #(doseq [f logger-fns]
     (f %)))
;= #'user/multi-logger

(def log (multi-logger
          (print-logger *out*)
          (file-logger "messages.log")))
;= #'user/log
(log "hello again")
; hello again
;= nil

;; a HOF that prepends timestamps to messages
(defn timestamped-logger [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s"
                   (java.util.Date.) %)))
;= #'user/timestamped-logger

(def log-timestamped (timestamped-logger
                      (multi-logger
                       (print-logger *out*)
                       (file-logger "messages.log"))))
;= #'user/log-timestamped
(log-timestamped "goodbye, now")
; [2014-03-11 09:25:10] goodbye, now
;= nil

;; Pure functions [ page 76 ]
