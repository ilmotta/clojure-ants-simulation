(ns demo.ant
  (:require [demo.config :refer [config]]
            [demo.world :as world]
            [demo.util :refer [bound rank-by wrand]]))

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

(defn next-direction [amount direction]
  (bound 8 (+ direction amount)))

(defn setup
  "Places initial food and ants, returns seq of ant agents."
  []
  (dosync
    (dotimes [i (config :food-places)]
      (world/set-food (world/rand-location) (rand-int (config :food-range))))
    (doall
      (for [x home-range y home-range]
        (do
          (world/set-home [x y])
          (world/create-ant (build-ant) [x y]))))))

(defn move
  "Moves the ant in the direction it is heading. Must be called in a
  transaction that has verified the way is clear."
  [location]
  (let [place (world/place location)
        ant (:ant @place)
        next-location (world/delta-loc location (:dir ant))
        next-place (world/place next-location)]
    (alter place dissoc :ant)
    (when-not (:home @place) (alter place update :pher inc))
    (alter next-place assoc :ant ant)
    next-location))

(defn turn
  "Turns the ant at the location by the given amount"
  [location amount]
  (alter (world/place location) update-in [:ant :dir] next-direction amount)
  location)

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

(defn close-places [location direction]
  (map world/place (world/close-locations location direction)))

(defn behave-loop
  "the main function for the ant agent"
  [location]
  (let [p (world/place location)
        ant (:ant @p)
        [ahead ahead-left ahead-right] (close-places location (:dir ant))
        places [ahead ahead-left ahead-right]]
    (. Thread (sleep (config :ant-sleep-ms)))
    (dosync
      (send-off *agent* behave-loop)
      (if (:food ant)
        ;going home
        (cond
          (:home @p)
          (-> location drop-food (turn 4))
          (and (:home @ahead) (not (:ant @ahead)))
          (move location)
          :else
          (let [ranks (merge-with +
                                  (rank-by (comp #(if (:home %) 1 0) deref) places)
                                  (rank-by (comp :pher deref) places))]
            (([move #(turn % -1) #(turn % 1)]
              (wrand [(if (:ant @ahead) 0 (ranks ahead))
                      (ranks ahead-left) (ranks ahead-right)]))
             location)))
        ;foraging
        (cond
          (and (pos? (:food @p)) (not (:home @p)))
          (-> location take-food (turn 4))
          (and (pos? (:food @ahead)) (not (:home @ahead)) (not (:ant @ahead)))
          (move location)
          :else
          (let [ranks (merge-with +
                                  (rank-by (comp :food deref) places)
                                  (rank-by (comp :pher deref) places))]
            (([move #(turn % -1) #(turn % 1)]
              (wrand [(if (:ant @ahead) 0 (ranks ahead))
                      (ranks ahead-left) (ranks ahead-right)]))
             location)))))))
