(ns demo.world
  (:require [demo.config :refer [config]]))

(defstruct ^:private cell
  :food :pher) ; May also have :ant and :home

(defn ^:private ref-to-cell [_]
  (ref (struct cell 0 0)))

(def x-range (range (config :dim)))
(def y-range (range (config :dim)))

(def ant? :ant)
(def food? (comp pos? :food))
(def pheromone? (comp pos? :pher))

; World is a 2d vector of refs to cells
(def ^:private world
  (mapv (fn [_] (mapv ref-to-cell y-range)) x-range))

(defn place [[x y]]
  (-> world (nth x) (nth y)))

(defn fetch-all-places []
  (vec (for [x x-range y y-range] @(place [x y]))))

(defn add-ant
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
