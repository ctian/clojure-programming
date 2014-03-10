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





