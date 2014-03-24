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







