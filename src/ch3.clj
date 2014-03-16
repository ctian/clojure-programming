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

;; creating seqs 
;; Generally, a sequence is produced by a collection, either explicity
;; via seq or via another function (like map) calling seq on its
;; arguments implicitly.

;; There are two ways to create a seq: cons and list*

(cons 0 (range 1 5))
;= (0 1 2 3 4)

(cons :a [:b :c :d])
;= (:a :b :c :d)

(cons 0 (cons 1 (cons 2 (cons 3 (range 4 10)))))
;= (0 1 2 3 4 5 6 7 8 9)

(list* 0 1 2 3 (range 4 10))
;= (0 1 2 3 4 5 6 7 8 9)

;; cons and list* are most commonly used when writing macros and when
;; assembling the next step of a lazy sequence.

;; Lazy seqs
(lazy-seq [1 2 3])
;= (1 2 3)

;; a sequence that lazily produces a sequence of random integers
(defn random-ints
  "Returns a lazy seq of random integers in the rang [0,limit)."
  [limit]
  (lazy-seq
   (cons (rand-int limit)
         (random-ints limit))))
;= #'user/random-ints

(take 10 (random-ints 50))
;= (27 33 16 20 25 22 45 49 7 43)

;; modify random-ints to print something every time a value is
;; realized
(defn random-ints [limit]
  (lazy-seq
   (println "realizing random number")
   (cons (rand-int limit)
         (random-ints limit))))
;= #'user/random-ints

(def rands (take 10 (random-ints 50)))
;= #'user/rands
(first rands)
; realizing random number
;= 31
(nth rands 3)
; realizing random number
; realizing random number
; realizing random number
;= 6
(count rands)
; realizing random number
; realizing random number
; realizing random number
; realizing random number
; realizing random number
; realizing random number
;= 10

;; cons and list* functions do NOT force the evaluation of the
;; squences they are provided as their final argument. This makes them
;; a key helper in building up lazy seqs.

;; a better practice is define random-ints from composing
;; standard-library Clojure functions together without construct lazy
;; sequence explicitly
(def random-ints (repeatedly 10 (partial rand-int 50)))
;= #'user/random-ints
random-ints
;= (5 29 26 1 37 6 32 45 24 43)

;; the expression provided to lazy-seq can do just about anything. Any
;; function implementing computation that is appropriate for a value
;; in a sequence is fair game.

;; all of the core sequence-processing functions in the standard
;; library - such as map, for, filter, take and drop - return lazy
;; sequence, and can be layered as needed without affecting the
;; laziness of underlying seqs.

;; next returns nil instead of an empty sequence, and this is only
;; possible because next checks for a non-empty tail seq. This check
;; forces the potential realization of the head of that nonempty tail
;; seq. In contrast, rest returns the tail of a given sequence,
;; thereby avoiding realizing its head and therefore maximizing
;; laziness.

;; sequential destructuring always uses next, and not rest.

;; force the complete realization of a lazy sequence with doall and
;; dorun. The difference is whether to retain the contents of the
;; sequence.

(dorun (take 5 random-ints))
;= nil
(doall (take 5 random-ints))
;= (5 29 26 1 37)

;; code defining lazy sequences should minimize side effects

(apply str (remove (set "aeiouy")
                   "vowels are useless! or maybe not..."))
;= "vwls r slss! r mb nt..."

;; head retention. In Clojure lazy sequences are persistent: an iterm
;; is computed once, but is still retained by sequence. As a result,
;; as long as you maintain a reference to a sequence, you'll prevent
;; its items from being garbage-collected.

(split-with neg? (range -5 5))
;= [(-5 -4 -3 -2 -1) (0 1 2 3 4)]

(let [[t d] (split-with #(< % 12) (range 1e8))]
  [(count d) (count t)])
; OutOfMemoryError GC overhead limit exceeded

(let [[t d] (split-with #(< % 12) (range 1e8))]
  [(count t) (count d)])
;= [12 99999988]

;; Associative
(def m {:a 1 :b 2 :c 3})
;= #'user/m
(get m :b)
;= 2
(get m :d)
;= nil
(get m :d "not-found")
;= "not-found"
(assoc m :d 4)
;= {:a 1, :c 3, :b 2, :d 4}
(dissoc m :b)
;= {:a 1, :c 3}
(assoc m
  :x 4
  :y 5
  :z 6)
;= {:z 6, :y 5, :x 4, :a 1, :c 3, :b 2}
(dissoc m :a :c)
;= {:b 2}

;; maps and vectors are both associative collections, where vectors
;; associate values with indices
(def v [1 2 3])
;= #'user/v
(get v 1)
;= 2
(get v 10)
;= nil
(get v 10 "not-found")
;= "not-found"
(assoc v
  1 4
  0 -12
  2 :p)
;= [-12 4 :p]
(assoc v 3 10)
;= [1 2 3 10]
(assoc v 10 :what)
;= IndexOutOfBoundException

;; get works on sets, where it retuns the "key" if it exists
(get #{1 2 3} 2)
;= 2
(get #{1 2 3} 4)
;= nil
(get #{1 2 3} 4 "not-found")
;= "not-found"

;; contains? is a predicate that returns true for an associative
;; collection if the given key is present.
(contains? [1 2 3] 0)
;= true
(contains? {:a 5 :b 6} :b)
;= true
(contains? {:a 5 :b 6} 6)
;= false
(contains? #{1 2 3} 1)
;= true

;; use some to check for the existence of a particular value in a
;; collection

(get "Clojure" 3)
;= \j
(contains? (java.util.HashMap.) "not-there")
;= false
(get (into-array [1 2 3]) 0)
;= 1

;; beware of nil, false values

;; find works like get except that it returns the whole entry, or, nil
;; when not found
(find {:ethel nil} :lucy)
;= nil
(find {:ethel nil} :ethel)
;= [:ethel nil]

;; find works well with destructuring and conditional forms like
;; if-let, or when-let
(if-let [e (find {:a 5 :b 6} :a)]
  (format "found %s => %s" (key e) (val e))
  "not found")
;= "found :a => 5"
(if-let [[k v] (find {:a 5 :b 6} :a)]
  (format "found %s => %s" k v)
  "not found")
;= "found :a => 5"

;; Indexed

;; excessive indexed lookup or modification is a code smell

;; nth is a specializationof get. They differ on how they deal with
;; out-fo-bounds indices: nth throws an exception while get returns
;; nil.

(nth [:a :b :c] 2)
;= :c
(get [:a :b :c] 2)
;= :c
(nth [:a :b :c] 3)
;= java.lang.IndexOutOfBoundsException
(get [:a :b :c] 3)
;= nil
(nth [:a :b :c] -1)
;= java.lang.IndexOutOfBoundsException
(get [:a :b :c] -1)
;= nil

;; when providing a default value, they are semantics identical
(nth [:a :b :c] -1 :not-found)
;= :not-found
(get [:a :b :c] -1 :not-found)
;= :not-found

;; when the subject of the lookup is not supported, get returns nil
;; while nth throws an exception
(get 42 0)
;= nil
(nth 42 0)
;= java.lang.UnsupportedOperationException: nth not supported on this
;type: Long

;; Clojure supports stack abstration via three operations: conj, pop,
;; and peek
;; Both lists and vectors can be used as stack
;; Use a list as a stack
(conj '() 1)
;= (1)
(conj '(2 1) 3)
;= (3 2 1)
(peek '(3 2 1))
;= 3
(pop '(3 2 1))
;= (2 1)
(pop '(1))
;= ()

;; Use a vector as a stack
(conj [] 1)
;= [1]
(conj [1 2] 3)
;= [1 2 3]
(peek [1 2 3])
;= 3
(pop [1 2 3])
;= [1 2]
(pop [1])
;= []

;; popping an empty stack will result in an error
(pop [])
;= IllegalStateException: can't pop empty vector
(pop ())
;= IllegalStateException: can't pop empty list

;; Set
(get #{1 2 3} 2)
;= 2
(get #{1 2 3} 4)
;= nil
(get #{1 2 3} 4 :not-found)
;- :not-found

;; use disj to remove value(s) from a set
(disj #{1 2 3} 3 1)
;= #{2}

(sort (keys (ns-publics 'clojure.set)))
;= (difference index intersection join map-invert project rename
;rename-keys select subset? superset? union)

;; Sorted. Only maps and set are available in sorted variants. They do
;; not have any literal notation; they may be created by sorted-map
;; and sorted-set, or sorted-map-by and sorted-set-by
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4))
;= #'user/sm
sm
;= {:a 3, :b 2, :c 4, :x 9, :y 0, :z 5}
(rseq sm)
;= ([:z 5] [:y 0] [:x 9] [:c 4] [:b 2] [:a 3])
(subseq sm <= :c)
;= ([:a 3] [:b 2] [:c 4])
(subseq sm > :b <= :y)
;= ([:c 4] [:x 9] [:y 0])
(rsubseq sm > :b <= :y)
;= ([:y 0] [:x 9] [:c 4])

;; compare function defines the default sort
(compare 3 3)
;= 0
(compare "ab" "abc")
;= -1
(compare ["a" "b" "c"] ["a" "b"])
;= 1
(compare ["a" 2] ["a" 2 0])
;= -1

;; Comparators and predicates to define ordering [ page 107 ]

;; compare supprts anything that implements java.lang.Comparable. It
;; is the default comparator.

;; All Clojure functions implement java.util.Comparator and can
;; therefore be used as comparators

;; examples using comparison functions
(sort < (repeatedly 10 #(rand-int 100)))
;= (17 19 19 19 32 63 64 77 85 97)
(sort-by first > (map-indexed vector "Clojure"))
;= ([6 \e] [5 \r] [4 \u] [3 \j] [2 \o] [1 \l] [0 \C])

;; use comparator function to turn a two-argument predicate into a
;; comparator function
((comparator <) 1 4)
;= -1
((comparator <) 4 1)
;= 1
((comparator <) 4 4)
;= 0

(sorted-map-by compare :z 5 :x 9 :y 0 :b 2 :a 3 :c 4)
;= {:a 3, :b 2, :c 4, :x 9, :y 0, :z 5}
(sorted-map-by (comp - compare) :z 5 :x 9 :y 0 :b 2 :a 3 :c 4)
;= {:z 5, :y 0, :x 9, :c 4, :b 2, :a 3}

;; sort order defines equality within a sorted map or set
(defn magnitude [x]
  (-> x Math/log10 Math/floor))
;= #'user/magnitude
(magnitude 100)
;= 2.0
(magnitude 123)
;= 2.0
(magnitude 1000000)
;= 6.0

;; create a comparison predicate using magnitude
(defn compare-magnitude [a b]
  (- (magnitude a) (magnitude b)))
;= #'user/compare-magnitude
((comparator compare-magnitude) 10 10000)
;= -1
;; why it returns -1 following two expressions ????
((comparator compare-magnitude) 10000 10)
;= -1  
((comparator compare-magnitude) 10 10)
;= -1

(sorted-set-by compare-magnitude 10 1000 500)
;= #{10 500 1000}
(conj *1 600)
;= #{10 500 1000}
(disj *1 750)
;= #{10 1000}
(contains? *1 1239)
;= true

;; rewrite compare-magnitude to make sure only equivalent numbers are
;; considered equal
(defn compare-magnitude [a b]
  (let [diff (- (magnitude a) (magnitude b))]
    (if (zero? diff)
      (compare a b)
      diff)))
;= #'user/compare-magnitude

(sorted-set-by compare-magnitude 10 1000 500)
;= #{10 500 1000}
(conj *1 600)
;= #{10 500 600 1000}
(disj *1 750)
;= #{10 500 600 1000}
(contains? *1 1239)
;= false

(def ss (sorted-set-by compare-magnitude 10 1000 500 670 1239))
;= #'user/ss
ss
;= #{10 500 670 1000 1239}
(subseq ss > 500)
;= (670 1000 1239)
(subseq ss > 500 <= 1000)
;= (670 1000)
(rsubseq ss > 500 <= 1000)
;= (1000 670)

;; implement linear imterpolate
(defn interpolate
  "Takes a collection of points (as [x y] tuples), returning a function
which is a linear interpolation between those points"
  [points]
  (let [results (into (sorted-map) (map vec points))]
    (fn [x]
      (let [[xa ya] (first (rsubseq results <= x))
            [xb yb] (first (subseq results > x))]
        (if (and xa xb)
          (/ (+ (* ya (- xb x)) (* yb (- x xa)))
             (- xb xa))
          (or ya yb))))))
;= #'user/interpolate

(def f (interpolate [[0 0] [10 10] [15 5]]))
;= #'user/f
(map f [2 10 12])
;= (2 10 8)

;; Concise collection access [ page 111 ]

















