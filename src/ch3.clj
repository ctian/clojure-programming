;; seq and conj are polymorphic over the type of collection they are
;; operating upon.

;; operations on a vector

(def v [1 2 3])
; #'user/v
(conj v 4)
;= [1 2 3 4]
(conj v 4 5)
;= [1 2 3 4 5]
(seq v)
;= (1 2 3)

;; operations on a map
(def m {:a 5 :b 6})
;= #'user/m
(conj m [:c 7])
;= {:a 5, :c 7, :b 6}
(seq m)
;= ([:a 5] [:b 6])

;; operations on a set
(def s #{1 2 3})
;= #'user/s
(conj s 10)
;= #{1 2 3 10}
(conj s 3 4)
;= #{1 2 3 4}
(seq s)
;= (1 2 3)

;; operations on a list
(def lst '(1 2 3))
;= #'user/lst
(conj lst 0)
;= (0 1 2 3)
(conj lst 0 -1)
;= (-1 0 1 2 3)
(seq lst)
;= (1 2 3)

;; into is built on top fo seq and conj that works on any values that
;; support seq and conj

(into v [4 5])
;= [1 2 3 4 5]
(into m [[:c 7] [:d 8]])
;= {:a 5, :c 7, :b 6, :d 8}
(into #{1 2} [2 3 4 5 3 3 2])
;= #{1 2 3 4 5}
(into [1] {:a 1 :b 2})
;= [1 [:a 1] [:b 2]]

;; Clojure encourages the use of unifying abstractions (sequences,
;; protocols, collection interfaces, etc). leaving you to have to
;; explicitly go out of your way to depend upon the specific behavior
;; of concrete types.

;; small and widely supported abstractions are one of the Clojure
;; design principles.

;; There are 7 different primary abstractions in which Clojure's data
;; structure implementations participate: Collection, Sequence,
;; Associattive, Indexed, Stack, Set, Sorted.







