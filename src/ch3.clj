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

;; Collection
;; a collection is a value that you can use with the set of core
;; collection functions: conj, seq, count, empty, =

;; specific action of conj is dependent upon the local characteristic
;; of the subject collection. But it always add a given value to the
;; subject collection efficiently.

;; empty generically work with collections of the same type as a given
;; instance.
;; a function that swap pairs of values in a sequential collection:
(defn swap-pairs [sequential]
  (into (empty sequential)
        (interleave
         (take-nth 2 (drop 1 sequential))
         (take-nth 2 sequential))))

(swap-pairs (apply list (range 10)))
;= (8 9 6 7 4 5 2 3 0 1)
(swap-pairs (apply vector (range 10)))
;= [1 0 3 2 5 4 7 6 9 8]

;; an example of using empty to work with a fresh instance of the
;; given concrete type. This allows the caller to determine the type
;; of values they get in return.
(defn map-map [f m]
  (into (empty m)
        (for [[k v] m]
          [k (f v)])))

(map-map inc (hash-map :z 5 :c 6 :a 0))
;= {:z 6, :a 1, :c 7}
(map-map inc (sorted-map :z 5 :c 6 :a 0))
;= {:a 1, :c 7, :z 6}

;; count guarantees efficient operation on all collections other than sequences
(count [1 2 3])
;= 3
(count {:a 1 :b 2 :c 3})
;= 3
(count #{1 2 3})
;= 3
(count '(1 2 3))
;= 3

;; count works on Java types that aren't Clojure collections, like
;; Strings, maps, collections, and arrays
(count "what")
;= 4
(def jal (java.util.ArrayList.))
(.add jal 1)
(.add jal 2)
(.add jal 3)
(.add jal 4)
(count jal)
;= 4

;; Sequence - the sequence abstraction defines a way to obtain and
;; traverse sequential views over some source of values.

;; seq produces a sequence over its argument
(seq "Clojure")
;= (\C \l \o \j \u \r \e)
(seq {:a 5 :b 6})
;= ([:a 5] [:b 6])
(seq (java.util.ArrayList. (range 6)))
;= (0 1 2 3 4 5)
(seq (into-array ["Clojure" "Programming"]))
;= ("Clojure" "Programming")
(seq [])
;= nil
(seq nil)
;= nil

;; A lot of functions that work with sequences call seq on their
;; argument(s) implicitly
(map str "Clojure")
;= ("C" "l" "o" "j" "u" "r" "e")
(set "Programming")
;= #{\a \g \i \m \n \o \P \r}

;; traversing sequences using first, rest, and next
(first "Clojure")
;= \C
(rest "Clojure")
;= (\l \o \j \u \r \e)
(next "Clojure")
;= (\l \o \j \u \r \e)

;; The difference between rest and next
;; rest will always return an empty sequence, whereas next will return
;; nil if the resulting sequence is empty
(rest [1])
;= ()
(next [1])
;= nil
(rest nil)
;= ()
(next nil)
;= nil

;; the following will always true for any value x
(= (next x)
   (seq (rest x)))

;; sequences are not iterators
(doseq [x (range 3)]
  (println x))
; 0
; 1
; 2
;= nil

(let [r (range 3)
      rst (rest r)]
  (prn (map str rst))
  (prn (map #(+ 100 %) r))
  (prn (conj r -1) (conj rst 42)))
; ("1" "2")
; (100 101 102)
; (-1 0 1 2) (42 1 2)
;= nil

;; sequences are not lists
;; getting the count of a list is a cheap, constant time operation
;; because lists track their length.Seqs cannot provide the same
;; guarantee, because they may be produced lazily and can potentially
;; be infinite. Thus, the only way to obtain a count of a seq is to
;; force a full traversal of it
(let [s (range 1e6)]
  (time (count s)))
; "Elapsed time: 142.974039 msecs"
;= 1000000

(let [s (apply list (range 1e6))]
  (time (count s)))
; "Elapsed time: 0.019556 msecs"
;= 1000000

;; creating seqs [ page 92 ]












