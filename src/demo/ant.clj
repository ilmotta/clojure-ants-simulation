(ns demo.ant
  (:require [demo.config :refer [config]]
            [demo.world :as world]))

(defstruct ant :dir) ; May also have :food

(def home-off
  (/ (config :dim) 4))

(def home-range
  (range home-off (+ (config :nants-sqrt) home-off)))

(defn food? [ant]
  (boolean (:food ant)))

(defn ant? [place]
  (boolean (:ant place)))

(defn build-ant []
  (struct ant (rand-int 8)))

(defn setup
  "Places initial food and ants, returns seq of ant agents"
  []
  (dosync
    (dotimes [i (config :food-places)]
      (world/set-food (world/rand-location) (rand-int (config :food-range))))
    (doall
      (for [x home-range y home-range]
        (do
          (world/set-home [x y])
          (world/add-ant (build-ant) [x y]))))))

(defn bound
  "returns n wrapped into range 0-b"
  [b n]
  (let [n (rem n b)]
    (if (neg? n)
      (+ n b)
      n)))

(defn wrand
  "given a vector of slice sizes, returns the index of a slice given a
  random spin of a roulette wheel with compartments proportional to
  slices."
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (loop [i 0 sum 0]
      (if (< r (+ (slices i) sum))
        i
        (recur (inc i) (+ (slices i) sum))))))

;dirs are 0-7, starting at north and going clockwise
;these are the deltas in order to move one step in given dir
(def dir-delta
  {0 [0 -1]
   1 [1 -1]
   2 [1 0]
   3 [1 1]
   4 [0 1]
   5 [-1 1]
   6 [-1 0]
   7 [-1 -1]})

(defn delta-loc
  "returns the location one step in the given dir. Note the world is a torus"
  [[x y] dir]
  (let [[dx dy] (dir-delta (bound 8 dir))]
    [(bound (config :dim) (+ x dx)) (bound (config :dim) (+ y dy))]))

(defn move
  "Moves the ant in the direction it is heading. Must be called in a
  transaction that has verified the way is clear"
  [location]
  (let [place (world/place location)
        ant (:ant @place)
        next-location (delta-loc location (:dir ant))
        next-place (world/place next-location)]
    (alter place dissoc :ant)
    (when-not (:home @place) (alter place update :pher inc))
    (alter next-place assoc :ant ant)
    next-location))

(defn next-direction [amount direction]
  (bound 8 (+ direction amount)))

(defn turn
  "Turns the ant at the location by the given amount"
  [location amount]
  (dosync
    (alter (world/place location) update-in [:ant :dir] next-direction amount))
  location)

(defn rank-by
  "returns a map of xs to their 1-based rank when sorted by keyfn"
  [keyfn xs]
  (let [sorted (sort-by (comp float keyfn) xs)]
    (reduce (fn [ret i] (assoc ret (nth sorted i) (inc i)))
            {} (range (count sorted)))))

(defn drop-food
  "Drops food at current location. Must be called in a transaction that has
  verified the ant has food."
  [location]
  (doto (world/place location)
    (alter update :food inc)
    (alter update :ant dissoc :food))
  location)

(defn take-food
  "Takes one food from current location. Must be called in a transaction that
  has verified there is food available."
  [location]
  (doto (world/place location)
    (alter update :food dec)
    (alter assoc-in [:ant :food] true))
  location)

(defn behave-loop
  "the main function for the ant agent"
  [loc]
  (let [p (world/place loc)
        ant (:ant @p)
        ahead (world/place (delta-loc loc (:dir ant)))
        ahead-left (world/place (delta-loc loc (dec (:dir ant))))
        ahead-right (world/place (delta-loc loc (inc (:dir ant))))
        places [ahead ahead-left ahead-right]]
    (. Thread (sleep (config :ant-sleep-ms)))
    (dosync
      (send-off *agent* behave-loop)
      (if (:food ant)
        ;going home
        (cond
          (:home @p)
          (-> loc drop-food (turn 4))
          (and (:home @ahead) (not (:ant @ahead)))
          (move loc)
          :else
          (let [ranks (merge-with +
                                  (rank-by (comp #(if (:home %) 1 0) deref) places)
                                  (rank-by (comp :pher deref) places))]
            (([move #(turn % -1) #(turn % 1)]
              (wrand [(if (:ant @ahead) 0 (ranks ahead))
                      (ranks ahead-left) (ranks ahead-right)]))
             loc)))
        ;foraging
        (cond
          (and (pos? (:food @p)) (not (:home @p)))
          (-> loc take-food (turn 4))
          (and (pos? (:food @ahead)) (not (:home @ahead)) (not (:ant @ahead)))
          (move loc)
          :else
          (let [ranks (merge-with +
                                  (rank-by (comp :food deref) places)
                                  (rank-by (comp :pher deref) places))]
            (([move #(turn % -1) #(turn % 1)]
              (wrand [(if (:ant @ahead) 0 (ranks ahead))
                      (ranks ahead-left) (ranks ahead-right)]))
             loc)))))))
