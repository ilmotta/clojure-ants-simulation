(ns demo.world
  (:require [demo.config :refer [config]]
            [demo.util :refer [bound]]))

(defstruct ^:private cell
  :food :pher :location) ; May also have :ant and :home

(defn ^:private ref-to-cell [x y]
  (ref (struct cell 0 0 [x y])))

;; dirs are 0-7, starting at north and going clockwise these are the deltas in
;; order to move one step in given dir.
(def dir-delta
  {0 [0 -1]
   1 [1 -1]
   2 [1 0]
   3 [1 1]
   4 [0 1]
   5 [-1 1]
   6 [-1 0]
   7 [-1 -1]})

(def x-range (range (config :dim)))
(def y-range (range (config :dim)))

(def ant? :ant)
(def food? (comp pos? :food))
(def pheromone? (comp pos? :pher))

; World is a 2d vector of refs to cells
(def ^:private world
  (mapv (fn [x] (mapv (partial ref-to-cell x) y-range)) x-range))

(defn place [[x y]]
  (-> world (nth x) (nth y)))

(defn fetch-all-places []
  (vec (for [x x-range y y-range] @(place [x y]))))

(defn create-ant
  "An ant agent tracks the location of an ant, and controls the behavior of
  the ant at that location. Must be called in a transaction."
  [ant location]
  (do
    (alter (place location) assoc :ant ant)
    (agent location)))

(defn set-home
  "Set given location as home. Must be called in a transaction."
  [location]
  (alter (place location) assoc :home true))

(defn rand-location []
  [(rand-int (config :dim)) (rand-int (config :dim))])

(defn set-food [location amount]
  (alter (place location) assoc :food amount))

(defn evaporate
  "Causes all the pheromones to evaporate a bit."
  []
  (dorun
    (for [x x-range y y-range]
      (alter (place [x y]) update :pher * (config :evaporation-rate)))))

(defn delta-loc
  "Returns the location one step in the given dir. Note the world is a torus."
  [[x y] direction]
  (let [[dx dy] (dir-delta (bound 8 direction))]
    [(bound (config :dim) (+ x dx)) (bound (config :dim) (+ y dy))]))

(defn close-locations [location direction]
  (map #(delta-loc location (% direction)) [identity dec inc]))

(defn update-place [deref-places]
  (last
    (doall
      (for [p (flatten [deref-places])]
        (ref-set (place (:location p)) p)))))
