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

(defn drop-pheromone [place]
  (update place :pher #(if (:home place) % (inc %))))

(defn move
  [from-place to-place]
  [(-> from-place (dissoc :ant) drop-pheromone)
   (assoc to-place :ant (:ant from-place))])

(defn turn [place amount]
  (update-in place [:ant :dir] next-direction amount))

(defn take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn drop-food [place]
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn close-places [location direction]
  (map world/place (world/close-locations location direction)))

(defn behave-loop
  "the main function for the ant agent"
  [location]
  (let [p (world/place location)
        ant (:ant @p)
        next-location (world/delta-loc location (:dir ant))
        [ahead ahead-left ahead-right] (close-places location (:dir ant))
        places [ahead ahead-left ahead-right]]
    (. Thread (sleep (config :ant-sleep-ms)))
    (dosync
      (send-off *agent* behave-loop)
      (if (:food ant)
        ;going home
        (cond
          (:home @p)
          (-> @p drop-food (turn 4) world/update-place :location)
          (and (:home @ahead) (not (:ant @ahead)))
          (-> @p (move @ahead) world/update-place :location)
          :else
          (let [ranks (merge-with + (rank-by (comp #(if (:home %) 1 0) deref) places) (rank-by (comp :pher deref) places))
                index (wrand [(if (:ant @ahead) 0 (ranks ahead)) (ranks ahead-left) (ranks ahead-right)])
                actions [#(move % @ahead) #(turn % -1) #(turn % 1)]]
            (-> @p ((actions index)) world/update-place :location)))
        ;foraging
        (cond
          (and (pos? (:food @p)) (not (:home @p)))
          (do
            (-> @p take-food (turn 4) world/update-place)
            (:location @p))
          (and (pos? (:food @ahead)) (not (:home @ahead)) (not (:ant @ahead)))
          (do
            (world/update-place (move @p @ahead))
            (:location @ahead))
          :else
          (let [ranks (merge-with + (rank-by (comp :food deref) places) (rank-by (comp :pher deref) places))
                index (wrand [(if (:ant @ahead) 0 (ranks ahead)) (ranks ahead-left) (ranks ahead-right)])
                actions [#(move % @ahead) #(turn % -1) #(turn % 1)]]
            (-> @p ((actions index)) world/update-place :location)))))))
