(ns demo.world
  (:require [demo.config :refer [config]]
            [demo.store :as store]
            [demo.util :refer [bound]]))

;; dirs are 0-7, starting at north and going clockwise these are the deltas in
;; order to move one step in given dir.
(def ^:private dir-delta
  {0 [0 -1]
   1 [1 -1]
   2 [1 0]
   3 [1 1]
   4 [0 1]
   5 [-1 1]
   6 [-1 0]
   7 [-1 -1]})

(def ^:private home-locations
  (doall (for [x (config :home-range) y (config :home-range)] [x y])))

(defn ^:private delta-loc
  "Returns the location one step in the given dir. Note the world is a torus."
  [[x y] direction]
  (let [[dx dy] (dir-delta (bound 8 direction))]
    [(bound (config :dim) (+ x dx)) (bound (config :dim) (+ y dy))]))

(defn ^:private rand-location [_]
  ((juxt rand-int rand-int) (config :dim)))

(defn rand-food-places []
  (map (comp store/place rand-location) (range (config :food-places))))

(defn home-places []
  (map store/place home-locations))

(defn nearby-places [location direction]
  (->> (map #(% direction) [identity dec inc])
       (map (partial delta-loc location))
       (map store/place)))

(defn evaporate [place]
  (update place :pher * (config :evaporation-rate)))

(def ant? (comp boolean :ant))
(def available? (comp not :ant))
(def food? (comp pos? :food))
(def home? (comp boolean :home))
(def pheromone? (comp pos? :pher))
