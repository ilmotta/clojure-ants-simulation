(ns demo.world
  (:require [demo.config :refer [config]]
            [demo.util :refer [bound]]))

(defstruct ^:private Cell
  :food :pher :location) ; May also have :ant and :home

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

(defn ^:private delta-loc
  "Returns the location one step in the given dir. Note the world is a torus."
  [[x y] direction]
  (let [[dx dy] (dir-delta (bound 8 direction))]
    [(bound (config :dim) (+ x dx)) (bound (config :dim) (+ y dy))]))

(def x-range (range (config :dim)))
(def y-range (range (config :dim)))

(def ant? :ant)
(def food? (comp pos? :food))
(def pheromone? (comp pos? :pher))

;; World is a 2D vector of refs to cells.
(def ^:private world
  (mapv (fn [x] (mapv #(ref (struct Cell 0 0 [x %])) y-range)) x-range))

(defn cell
  ([] (vec (for [x x-range y y-range] (cell [x y]))))
  ([[x y]] (-> world (nth x) (nth y))))

(defn place
  ([] (mapv deref (cell)))
  ([cell] @cell))

(defn ^:private rand-place [_]
  @(cell ((juxt rand-int rand-int) (config :dim))))

(defn evaporate []
  (map #(alter % update :pher * (config :evaporation-rate)) (cell)))

(defn home-places []
  (doall
    (for [x (config :home-range) y (config :home-range)]
      @(cell [x y]))))

(defn rand-food-places []
  (map rand-place (range (config :food-places))))

(defn update-place [places]
  (cell (:location (last (doall (map #(ref-set (cell (:location %)) %) (flatten [places])))))))

(defn nearby-places [location direction]
  (->> (map #(% direction) [identity dec inc])
       (map (partial delta-loc location))
       (map (comp deref cell))))
