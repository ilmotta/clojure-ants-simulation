(ns demo.world
  (:require [demo.config :refer [config]]
            [demo.domain :refer [build-ant]]
            [demo.store :as store]
            [demo.util :refer [bound]]))

(def ^:private setup-food
  (partial map #(assoc % :food (rand-int (config :food-range)))))

(def ^:private setup-home
  (partial map #(assoc % :home true)))

(def ^:private setup-ants
  (partial map #(assoc % :ant (build-ant {:dir (rand-int 8) :agent (agent (:location %))}))))

(def ^:private direction-delta
  {0 [0 -1], 1 [1 -1], 2 [1 0], 3 [1 1] 4 [0 1], 5 [-1 1], 6 [-1 0], 7 [-1 -1]})

(def ^:private home-locations
  (doall (for [x (config :home-range) y (config :home-range)] [x y])))

(def ^:private bound-location
  (partial map (partial bound (config :dim))))

(defn ^:private delta-location [location direction]
  (->> (bound 8 direction) direction-delta (map + location) bound-location))

(defn ^:private rand-location [_]
  ((juxt rand-int rand-int) (config :dim)))

(defn ^:private home-places []
  (map store/place home-locations))

(defn ^:private rand-food-places []
  (map (comp store/place rand-location) (range (config :food-places))))

(defn evaporate [place]
  (update place :pher * (config :evaporation-rate)))

(defn nearby-places [location direction]
  (->> (map #(% direction) [identity dec inc])
       (map (partial delta-location location))
       (map store/place)))

(defn setup []
  (-> (home-places) setup-home setup-ants store/update-place)
  (-> (rand-food-places) setup-food store/update-place))

(def ant? (comp boolean :ant))
(def available? (comp not :ant))
(def food? (comp pos? :food))
(def home? (comp boolean :home))
(def pheromone? (comp pos? :pher))
