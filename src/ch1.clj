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

