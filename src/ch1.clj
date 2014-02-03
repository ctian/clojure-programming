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

;;;;;; Symbols ( page 15 )



