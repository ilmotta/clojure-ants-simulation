(ns demo.store
  (:require [demo.config :refer [config]]))

(defrecord ^:private Cell [food pher location ant home])

;; World is a 2D vector of refs to cells.
(def ^:private world
  (mapv (fn [x] (mapv #(ref (map->Cell {:food 0 :pher 0 :location [x %]}))
                      (config :y-range)))
        (config :x-range)))

(defn ^:private cell
  ([] (vec (for [x (config :x-range) y (config :y-range)] (cell [x y]))))
  ([[x y]] (-> world (nth x) (nth y))))

(defn place
  ([] (mapv deref (cell)))
  ([location] @(cell location)))

(defn update-place [places]
  (->> (flatten [places])
       (map #(ref-set (cell (:location %)) %))
       ((comp place :location last))))