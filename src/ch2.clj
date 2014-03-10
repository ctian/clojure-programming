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

;; A critial choice [ page 58 ]

