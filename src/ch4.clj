;; a delay is a construct that suspends some body of code, evaluating
;; it only upon demand when it is dereferenced
(def d (delay (println "Running...")
              :done!))
;= #'user/d
(deref d)
; Running...
;= :done!

;; accomplish the same sort of thing using function
(def a-fn (fn []
            (println "Running...")
            :done!))
;= #'user/a-fn
(a-fn)
; Running...
;= :done!

;; Delays only evaluate their body of code once, caching the return
;; value.
@d
;= :done!

;; A corollary to this is that multiple threads can safely attempt to
;; dereference a delay for the first time; all of them will block
;; until the delay's code is evaluated (only once!), and a value is
;; available

;; offering opt-in computation with a delay
(defn get-document [id]
  ; .. do some work to retrieve the identified document's metadata ...
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (delay (slurp "http://www.mozilla.org/about/manifesto.en.html"))})
;= #'user/get-document

(def d (get-document "some-id"))
;= #'user/d
d
;= {:url "http://www.mozilla.org/about/manifesto.en.html", :title "The Mozilla Manifesto", :mime "text/html", :content #<Delay@72e6f103: :pending>}

(realized? (:content d))
;= false
@(:content d)
; failed with error: java.net.UnknownHostException: www.mozilla.org
(realized? (:content d))
;= false

;; realized? may also be used with futures, promises, and lazy
;; sequences.

;; A Clojure future evaluates a body of code in another thread
(def long-calculation (future (apply + (range 1e8))))
;= #'user/long-calculation

;; future return immediately, allowing the current thread of execution
;; to carry on. The result of evaluation will be retained by the
;; future, which can be obtained by dereferencing.
@long-calculation
;= 4999999950000000

;; dereferencing a future will block if the code it is evaluating has
;; not completed yet.
@(future (Thread/sleep 5000) :done!)
;= :done!

;; provide a timeout and a "timeout value" when dereferencing a future
(deref (future (Thread/sleep 5000) :done)
       1000
       :impatient!)
;= :impatient!

;; future are often used to simplify the usage of APIs that perform
;; some concurrent aspect to their operation.
(defn get-content [id]
  ; .. do some work to retrieve the identified document's metadata ..
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (future (slurp "http://www.mozilla.org/about/manifesto.en.html"))})
;= #'user/get-content
(get-content "some-id")
;= {:url "http://www.mozilla.org/about/manifesto.en.html", :title "The Mozilla Manifesto", :mime "text/html", :content #<core$future_call$reify__6267@1ccbee0d: :pending>}
@(:content (get-content "some-id"))
; java.util.concurrent.ExecutionException: java.net.UnknownHostException: www.mozilla.org

;; promise is initially a barren container. At some later point in
;; time, the promise may be fulfilled by having a value delivered to
;; it
(def p (promise))
;= #'user/p
(realized? p)
;= false
(deliver p 42 )
;= #<core$promise$reify__6310@4b754845: 42>
(realized? p)
;= true
@p
;= 42

;; a promise is similar to a one-time, single-vale pipe: data is
;; inserted at one end via deliver and retrieved at the other end by
;; deref. It is sometimes called dataflow variable.
(def a (promise))
(def b (promise))
(def c (promise))

;; create a future to specify how these promises are related
(future
  (deliver c (+ @a @b))
  (println "Delivery complete!"))
;= #<core$future_call$reify__6267@51b343e9: :pending>
(deliver a 15)
;= #<Promise@1fb5dddf: 15>
(deliver b 16)
; Delivery complete!
;= #<Promise@4ada213a: 16>
@c
;= 31

;; promises don't detect cyclic dependencies

;; a practical application of promises is in easily making call-based
;; APIs synchronous
(defn call-service
  [arg1 arg2 callback-fn]
  ; ... perform service call, eventually invoking callback-fn with results...
  (future (callback-fn (+ arg1 arg2) (- arg1 arg2))))

(defn sync-fn [async-fn]
  (fn [& args]
    (let [result (promise)]
      (apply async-fn (conj (vec args) #(deliver result %&)))
      @result)))

((sync-fn call-service) 8 7)
;= (15 1)

(defn phone-numbers [string]
  (re-seq #"(\d{3})[\.-]?(\d{3})[\.-]?(\d{4})" string))
;= #'user/phone-numbers

(phone-numbers " Sunil: 617.555.2937, Betty: 508-555-2218")
;= (["617.555.2937" "617" "555" "2937"] ["508-555-2218" "508" "555"
;"2218"])

;; dummy up a seq of 100 strings, each about 1MB in size, suffixed
;; with some phone numbers
(def files (repeat 100
                   (apply str
                          (concat (repeat 1000000 \space)
                                  "Sunil: 617.555.2937, Betty: 508-555-2218"))))
;= #'user/files

(time (dorun (map phone-numbers files)))
; "Elapsed time: 1617.142375 msecs"
;= nil

(time (dorun (pmap phone-numbers files)))
; "Elapsed time: 720.338371 msecs"
;= nil

;; pmap is using a number of futures to spread the computation across
;; each of those cores.

;; There is a degree of overhead associated with parallelizing
;; operations. If the operaton being parallelized does not have a
;; significant enough runtime, that overhead will dominate the real
;; work being performed.

(def files (repeat 1000000
                   (apply str
                          (concat (repeat 1000 \space)
                                  "Sunil: 617.555.2937, Betty: 508-555-2218"))))
;= #'user/files

(time (dorun (map phone-numbers files)))
; "Elapsed time: 27795.852547 msecs"
;= nil
(time (dorun (pmap phone-numbers files)))
; "Elapsed time: 29082.383511 msecs"
;= nil

;; efficiently parallelize a relatively trival operation by chunking
;; the dataset so that each unit of parallelized work is larger.
(time (->> files
           (partition-all 250)
           (pmap (fn [chunk] (doall (map phone-numbers chunk))))
           (apply concat)
           (dorun)))
; "Elapsed time: 8794.456556 msecs"
;= nil

;; pcalls and pvalues are the two other parallelism constructs built
;; on top of pmap

;; Identifies are represented in Clojure using reference types: vars,
;; refs, agents, and atoms.

;; references are boxes that hold a value

;; accessing the value contained by a reference using deref or @
@(atom 12)
;= 12
@(agent {:c 42})
;= {:c 42}
(map deref [(agent {:c 42}) (atom 12) (ref "http://clojure.org") (var +)])
;= ({:c 42} 12 "http://clojure.org" #<core$_PLUS_
;clojure.core$_PLUS_@434f908c>)

;; dereferencing returns a snapshot of the state of a reference when
;; deref is invoked.

;; deref will never block. Dereferencing a reference type will never
;; interfere with other operations.

;; a macro that produces code that creates n futures for each
;; expression provided to the macro to be evaluated
(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))
;= #'user/futures

;; the following macro provide the same capabilities as futures, but
;; always return nil and block until the futures are all realized
(defmacro wait-futures [& args]
  `(doseq [f# (futures ~@args)]
     @f#))
;= #'user/wait-futures

;; an atom is an identity that implement synchronous, uncoordinated,
;; atomic compare-and-set modification.

;; create an atom with atom and modify it with swap!
(def sarah (atom {:name "Sarah" :age 35 :wear-glasses? false}))
;= #'user/sarah
(swap! sarah update-in [:age] + 3)
;= {:age 38, :name "Sarah", :wear-glasses? false}

;; modification of an atom occurs atomically
(swap! sarah (comp #(update-in % [:age] inc)
                   #(assoc % :wear-glasses? true)))
;= {:age 39, :name "Sarah", :wear-glasses? true}

;; if the atom's value changes before the update function returns,
;; swap! will retry, calling the update function again with the atom's
;; newer value.

(def xs (atom #{1 2 3}))
;= #'user/xs
(wait-futures 1 (swap! xs (fn [v]
                            (Thread/sleep 250)
                            (println "trying 4")
                            (conj v 4)))
              (swap! xs (fn [v]
                          (Thread/sleep 500)
                          (println "trying 5")
                          (conj v 5))))
; trying 4
; trying 5
; trying 5
;= nil
@xs
;= #{1 2 3 4 5}

;; function that changes an atom value does not return until it has
;; completed because atom is a synchronous reference type
(def x (atom 2000))
;= #'user/x
(swap! x #(Thread/sleep %))  ; takes at least 2 seconds to return
;= nil

(compare-and-set! xs :wrong "new value")
;= false
@xs
;= #{1 2 3 4 5}
(compare-and-set! xs @xs "new value")
;= true
@xs
;= "new value"

;; compare-and-set! does NOT use value semantics, it requires that the
;; value in the atom be identical to the expected value provided to it
;; as its second argument
(def xs (atom #{1 2}))
;= #'user/xs
(compare-and-set! xs #{1 2} "new value")
;= false
@xs
;= #{1 2}

;; set the state of an atom without regard for what it contains
;; currently using reset!
(reset! xs :y)
;= :y
@xs
;= :y

;; two facilities that all reference types support: notifications and
;; constraints

;; watches are functions called whenever the state of a reference has
;; changed.

(defn echo-watch
  [key identity old new]
  (println key old "=>" new))
;= #'user/echo-watch

(def sarah (atom {:name "Sarah" :age 25}))
;= #'user/sarah
(add-watch sarah :echo echo-watch)
;= #<Atom@5019f68: {:age 25, :name "Sarah"}>
(swap! sarah update-in [:age] inc)
; :echo {:age 25, :name Sarah} => {:age 26, :name Sarah}
;= {:age 26, :name "Sarah"}
(add-watch sarah :echo2 echo-watch)
;= #<Atom@574c2605: {:age 26, :name "Sarah"}>
(swap! sarah update-in [:age] inc)
; :echo {:age 26, :name Sarah} => {:age 27, :name Sarah}
; :echo2 {:age 26, :name Sarah} => {:age 27, :name Sarah}
;= {:age 27, :name "Sarah"}

;; watch functions are called synchronously on the same thread that
;; changes the reference's state

;; the key you provide to add-watch can be used to remove the watch
(remove-watch sarah :echo2)
;= #<Atom@574c2605: {:age 27, :name "Sarah"}>
; :echo {:age 27, :name Sarah} => {:age 28, :name Sarah}
;= {:age 28, :name "Sarah"}

;; watches on a reference type are called whenever the reference's
;; state is modified, not necessarily different
(reset! sarah @sarah)
; :echo {:age 28, :name Sarah} => {:age 28, :name Sarah}
;= {:age 28, :name "Sarah"}

(def history (atom ()))
;= #'user/history

(defn log->list [dest-atom key source old new]
  (when (not= old new)
    (swap! dest-atom conj new)))
;= #'user/log->list

(def sarah (atom {:name "Sarah" :age 25}))
;= #'user/sarah
(add-watch sarah :record (partial log->list history))
;= #<Atom@6f89cd7e: {:age 25, :name "Sarah"}>
(swap! sarah update-in [:age] inc)
;= {:age 26, :name "Sarah"}
(swap! sarah update-in [:age] inc)
;= {:age 27, :name "Sarah"}
(swap! sarah identity)
;= {:age 27, :name "Sarah"}
(swap! sarah assoc :wear-glasses? true)
;= {:age 27, :name "Sarah", :wear-glasses? true}
(swap! sarah update-in [:age] inc)
;= {:age 28, :name "Sarah", :wear-glasses? true}
(pprint @history)
(comment 
({:age 28, :name "Sarah", :wear-glasses? true}
 {:age 27, :name "Sarah", :wear-glasses? true}
 {:age 27, :name "Sarah"}
 {:age 26, :name "Sarah"})
)
;= nil

;; make the behavior of the watch function vary depending on the key
;; it's registered under.
(defn log->db
  [db-id identity old new]
  (when (not= old new)
    (let [db-connection (get-connection db-id)]
      ;; ...
      )))

(add-watch sarah "jdbc:postgresql://hostname/some_database" log->db)

;; a validator is a function of a single argument that is invoked just
;; before any proposed new state is installed into a reference.
(def n (atom 1 :validator pos?))
;= #'user/n
(swap! n + 500)
;= 501
(swap! n - 1000)
; java.lang.IllegalStateException: Invalid reference state

;; add/change validator with set-validator!
(def sarah (atom {:name "Sarah" :age 25}))
;= #'user/sarah
(set-validator! sarah :age)
;= nil
(swap! sarah dissoc :age)
; java.lang.IllegalStateException: Invalid reference state

(set-validator! sarah #(or (:age %)
                           (throw (IllegalStateException. "People must have ':age's!"))))
;= nil
(swap! sarah dissoc :age)
; java.lang.IllegalStateException: People must have ':age's!

;; Refs [ page 180 ]







