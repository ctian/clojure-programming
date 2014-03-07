(defn average [numbers]
  (/ (apply + numbers) (count numbers)))

(average [60 80 100 400])
;= 160

(read-string "42")
;= 42

(read-string "(+ 1 2)")
;= (+ 1 2)

(pr-str [1 2 3])
;= "[1 2 3]"

(read-string "[1 2 3]")
;= [1 2 3]

"multiline string
are very handy"
;= "multiline string\nare very handy"

(class \c)
;= java.lang.Character

\u00ff
;= \ÿ

\o41
;= \!

;;;;;; Keywords

(def person {:name "Sandra Cruz"
             :city "Portland, ME"})
;= #'user/person
(:city person)
;= "Portland, ME"

;; a keyword prefixed with two colons (::) is expanded
;; by the reader to a name-spaced keyword in the current namespace
;; or another namespace if the keyword started by a namespace alias
;; like ::alias/kw
(def pizza {:name "Ramunto's"
            :location "Claremont, NH"
            ::location "43-3734,-72.3365"})
;= #'user/pizza
pizza
;= {:name "Ramunto's", :location "Claremont, NH", :user/location "43-3734,-72.3365"}
(:user/location pizza)
;= "43-3734,-72.3365"

(name :user/location)
;= "location"
(namespace :user/location)
;= "user"
(namespace :location)
;= nil

0xff
;= 255

040
;= 32

2r111
;= 7

16rff
;= 255

8r12
;= 10

(type 10)
;= java.lang.Long

(type 10N)
;= clojure.lang.BigInt

(type 1.2)
;= java.lang.Double

(type 1.2M)
;= java.math.BigDecimal

(type 1/2)
;= clojure.lang.Ratio

3/12
;= 1/4

(type #"(p|h)ail")
;= java.util.regex.Pattern

(re-seq #"(...) (...)" "foo bar")
;= (["foo bar" "foo" "bar"])

(re-seq #"(\d+)-(\d+)" "1-3")  ;; would be "(\\d+)-(\\d+)" in java
;= (["1-3" "1" "3"])

(javadoc java.util.regex.Pattern)

javadoc
;= #<javadoc$javadoc clojure.java.javadoc$javadoc@68c0ec3>

;; form-level comments using #_ reader macro
(read-string "(+ 1 2 #_(* 2 2) 8)")
;= (+ 1 2 8)

;; comment forms always evaluate to nil
(+ 1 2 (comment (* 2 2)) 8)
;= NullPointerException   clojure.lang.Numbers.ops

(+ 1 2 #_(* 2 2) 8)
;= 11

;; commas are considered whitespace by the reader
(= [1 2 3] [1, 2, 3])
;= true

#'javadoc
;= #'clojure.java.javadoc/javadoc

`javadoc
;= clojure.java.javadoc/javadoc

;; Namespaces (page 20)

(def x 1)
;= #'user/x

x
;= 1

(def x "hello")
;= #'user/x

x
l= "hello"

;; find current namespace
*ns*
;= #<Namespace user>

;; create a new namespace. It has the side effect of switching to that
;; new namespace in REPL
(ns foo)
;= nil

*ns*
;= #<Namespace foo>

user/x
;= "hello"

x
;= #<CompilerException java.lang.RuntimeException:
;= Unable to resolve symbol: x in this context, compiling:(NO_SOURCE_PATH:0)>

;; (p 22) We mentioned earlier that ....

String
;= java.lang.String

Integer
;= java.lang.Integer

java.util.List
;= java.util.List

java.net.What
; java.lang.ClassNotFoundException: java.net.What

java.net.Socket
;= java.net.Socket

filter
;= #<core$filter clojure.core$filter@234a98fa>

(quote x)
;= x
(symbol? (quote x))
;= true

'x
;= x

'(+ x x)
;= (+ x x)

(list? '(+ x x))
;= true

;; peek what the reader produce by quoting a form

''x
;= (quote x)

'#'x
;= (var x)

'@x
;= (clojure.core/deref x)

'#(+ % %)
;= (fn* [p1__1148#] (+ p1__1148# p1__1148#))


'`(a b ~c)
;= (clojure.core/seq (clojure.core/concat (clojure.core/list (quote
;foo/a)) (clojure.core/list (quote foo/b)) (clojure.core/list c)))

(do
  (println "hi")
  (apply * [4 5 6]))
; hi
;= 120

(let [a (inc (rand-int 6))
      b (inc (rand-int 6))]
  (println (format "You rolled a %s and a %s" a b))
  (+ a b))
;= 5

(defn hypot [x y]
  (let [x2 (* x x)
        y2 (* y y)]
    (Math/sqrt (+ x2 y2))))

(hypot 3 4)

(def v [42 "foo" 99.2 [5 12]])

(first v)
(second v)
(last v)
(v 2)
(nth v 2)
(.get v 2)

(let [[x y z] v]
  (+ x z))
;= 141.2

;; the above is equivalent to
(let [x (nth v 0)
      y (nth v 1)
      z (nth v 2)]
  (+ x z))
;= 141.2

;; destructuring forms can be composed
(let [[x _ _ [y z]] v]
  (+ x y z))
;= 59

;; gathering extra-positional sequential values
(let [[x & rest] v]
  rest)
;= ("foo" 99.2 [5 12])

;; retaining the destructured value
(let [[x _ z :as original-vector] v]
  (conj original-vector (+ x z)))
;= [42 "foo" 99.2 [5 12] 141.2]

(def m {:a 5 :b 6
        :c [7 8 9]
        :d {:e 10 :f 11}
        "foo" 88
        42 false})

(let [{a :a b :b} m]
  (+ a b))
;= 11

;; any type of value may be used for lookup
(let [{f "foo"} m]
  (+ f 12))
;= 100

(let [{v 42} m]
  (if v 1 0))
;= 0

;; Indices into vectors, strings, and arrays
;; can be used as key in a map destructuring form
(let [{x 3 y 8} [12 0 0 -18 44 6 0 0 1]]
  (+ x y))
;= -17

(let [{{ e :e} :d} m]
  (* 2 e))
;= 20

(let [{[x _ y] :c} m]
  (+ x y))
;= 16

(def map-in-vector ["James" {:birthday (java.util.Date. 73 1 6)}])
(let [[name {bd :birthday}] map-in-vector]
  (str name " was born on " bd))
;= "James was born on Tue Feb 06 00:00:00 EST 1973"

;; retaining the destructured value
(let [{r1 :x r2 :y :as randoms}
      (zipmap [:x :y :z] (repeatedly (partial rand-int 10)))]
  (assoc randoms :sum (+ r1 r2)))
;= {:sum 6, :z 9, :y 0, :x 6}

;; default values
(let [{k :unknown x :a
       :or {k 50}} m]
  (+ k x))
;= 55

(let [{k :unknown x :a} m
      k (or k 50)]
  (+ x k))
;= 55

(let [{opt1 :option} {:option false}
      opt1 (or opt1 true)
      {opt2 :option :or {opt2 true}} {:option false}]
  {:opt1 opt1 :opt2 opt2})
;= {:opt1 true, :opt2 false}

;; binding values to their keys' names
(def chas {:name "Chas" :age 31 :location "massachusetts"})

(let [{name :name age :age location :location} chas]
  (format "%s is %s years old and lives in %s." name age location))
;= "Chas is 31 years old and lives in massachusetts."

(let [{:keys [name age location]} chas]
  (format "%s is %s years old and lives in %s." name age location))
;= "Chas is 31 years old and lives in massachusetts."

(def brian {"name" "Brian" "age" 31 "location" "British Columbia"})
(let [{:strs [name age location]} brian]
  (format "%s is %s years old and lives in %s." name age location))
;= "Brian is 31 years old and lives in British Columbia."

(def christophe {'name "Christophe" 'age 33 'location "Rhone-Alpes"})
(let [{:syms [name age location]} christophe]
  (format "%s is %s years old and lives in %s." name age location))
;= "Christophe is 33 years old and lives in Rhone-Alpes."

(def  user-info ["robert8990" 2011 :name "Bob" :city "Boston"])

(let [[username account-year & extra-info] user-info
      {:keys [name city]} (apply hash-map extra-info)]
  (format "%s is in %s" name city))
;= "Bob is in Boston"

;; map destructuring of rest seqs
(let [[username account-year & {:keys [name city]}] user-info]
  (format "%s is in %s" name city))
;= "Bob is in Boston"

;; create function using fn
(fn [x]
  (+ 10 x))
;= #<foo$eval1095$fn__1096 foo$eval1095$fn__1096@46e9e26a>

((fn [x] (+ 10 x)) 8)
;= 18

;; the above function call is equivalent to:
(let [x 8] (+ 10 x))
;= 18

((fn [x y z] (+ x y z)) 3 4 12)
;- 19

;; the function call is the equivalent of the following let form
(let [x 3
      y 4
      z 12]
  (+ x y z))
;= 19

;; function with multiple arities
(def strange-adder (fn adder-self-reference
                     ([x] (adder-self-reference x 1))
                     ([x y] (+ x y))))
;= #'foo/strange-adder
(strange-adder 10)
;= 11
(strange-adder 10 50)
;= 60










