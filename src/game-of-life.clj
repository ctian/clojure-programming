;; traditional way

(defn empty-board
  "Creates a rectangular empty board of the specified width and height."
  [w h]
  (vec (repeat w (vec (repeat h nil)))))
;= #'user/empty-board

(defn populate
  "Turns :on each of the cells specified as [y, x] coordinates."
  [board living-cells]
  (reduce (fn [board coordinates]
            (assoc-in board coordinates :on))
          board
          living-cells))
;= #'user/populate

(def glider (populate (empty-board 6 6) #{[2 0] [2 1] [2 2] [1 2] [0 1]}))

(pprint glider)
; [[nil :on nil nil nil nil]
;  [nil nil :on nil nil nil]
;  [:on :on :on nil nil nil]
;  [nil nil nil nil nil nil]
;  [nil nil nil nil nil nil]
;  [nil nil nil nil nil nil]]
;= nil 

;; take a board's state, return its successor according to the game's
;; rules

(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))
;= #'user/neighbours

(neighbours [2 2])
;=([1 1] [1 2] [1 3] [2 1] [2 3] [3 1] [3 2] [3 3])

(defn count-neighbours [board loc]
  (count (filter #(get-in board %) (neighbours loc))))
;= #'user/count-neighbours

(defn indexed-step
  "Yields the next state of the board, using indices to determine neighbours, liveness, etc."
  [board]
  (let [w (count board)
        h (count (first board))]
    (loop [new-board board x 0 y 0]
      (cond
       (>= x w) new-board
       (>= y h) (recur new-board (inc x) 0)
       :else
       (let [new-liveness
             (case (count-neighbours board [x y])
               2 (get-in board [x y])
               3 :on
               nil)]
         (recur (assoc-in new-board [x y] new-liveness) x (inc y)))))))
;= #'user/indexed-step

(-> (iterate indexed-step glider) (nth 8) pprint)
; [[nil nil nil nil nil nil]
;  [nil nil nil nil nil nil]
;  [nil nil nil :on nil nil]
;  [nil nil nil nil :on nil]
;  [nil nil :on :on :on nil]
;  [nil nil nil nil nil nil]]
;= nil

;; rework to avoid indices
(defn indexed-step2
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board x]
       (reduce
        (fn [new-board y]
          (let [new-liveness
                (case (count-neighbours board [x y])
                  2 (get-in board [x y])
                  3 :on
                  nil)]
            (assoc-in new-board [x y] new-liveness)))
        new-board (range h)))
     board (range w))))
;= #'user/indexed-step2

(-> (iterate indexed-step2 glider) (nth 8) pprint)

;; collapse the reduces
(defn indexed-step3
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board [x y]]
       (let [new-liveness
             (case (count-neighbours board [x y])
               2 (get-in board [x y])
               3 :on
               nil)]
         (assoc-in new-board [x y] new-liveness)))
     board (for [x (range h) y (range w)] [x y]))))
;= #'user/indexed-step3

;; use sequences to replace indices
(partition 3 1 (range 5))
;= ((0 1 2) (1 2 3) (2 3 4))

(defn window
  "Returns a lazy sequence of 3-item windows centered
around each item of coll, padded as necessary with
pad or nil"
  ([coll] (window nil coll))
  ([pad coll] (partition 3 1 (concat [pad] coll [pad]))))
;= #'user/window

(window (range 5))
;= ((nil 0 1) (0 1 2) (1 2 3) (2 3 4) (3 4 nil))

(defn cell-block
  "Creates a sequences of 3x3 windows from a triple of 3 sequences."
  [[left mid right]]
  (window (map vector left mid right)))
;= #'user/cell-block

(cell-block [[0 1] [1 1] [1 2]])
;= ((nil [0 1 1] [1 1 2]) ([0 1 1] [1 1 2] nil))
(cell-block [[0 1] [2 2] [2 5]])
;= ((nil [0 2 2] [1 2 5]) ([0 2 2] [1 2 5] nil))

(defn liveness
  "Returns the liveness (nil or :on) of the center cell for
the next step."
  [block]
  (let [[_ [_ center _ ]_] block]
    (case (- (count (filter #{:on} (apply concat block)))
             (if (= :on center) 1 0))
      2 center
      3 :on
      nil)))
;= #'user/liveness


