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
  (doall (for [x (config :home-range)
               y (config :home-range)]
           [x y])))

(defn ^:private delta-loc
  "Returns the location one step in the given dir. Note the world is a torus."
  [[x y] direction]
  (let [[dx dy] (dir-delta (bound 8 direction))]
    [(bound (config :dim) (+ x dx)) (bound (config :dim) (+ y dy))]))

(defn ^:private rand-location []
  ((juxt rand-int rand-int) (config :dim)))

(defn ^:private rand-place [_]
  (store/place (rand-location)))

(defn rand-food-places []
  (map rand-place (range (config :food-places))))

(defn home-places []
  (map store/place home-locations))

(defn nearby-places [location direction]
  (->> (map #(% direction) [identity dec inc])
       (map (partial delta-loc location))
       (map store/place)))

(defn has-food-and-not-home? [place]
  (and (pos? (:food place)) (not (:home place))))

(defn home-and-available? [place]
  (and (:home place) (not (:ant place))))

(defn evaporate [places]
  (map #(update % :pher * (config :evaporation-rate)) places))

(def ant? :ant)
(def food? (comp pos? :food))
(def pheromone? (comp pos? :pher))
