(ns demo.ant
  (:require [demo.util :refer [bound rank-by roulette]]
            [demo.world :as world]))

(defn ^:private drop-food [place]
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn ^:private trail [place]
  (update place :pher #(if (world/home? place) % (inc %))))

(defn ^:private move [from-place to-place]
  [(trail (dissoc from-place :ant))
   (assoc to-place :ant (:ant from-place))])

(defn ^:private take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn ^:private turn [place amount]
  (update-in place [:ant :dir] (comp (partial bound 8) +) amount))

(def ^:private rank-by-pher (partial rank-by :pher))
(def ^:private rank-by-home (partial rank-by #(if (:home %) 1 0)))
(def ^:private rank-by-food (partial rank-by :food))
(def ^:private foraging (juxt rank-by-food rank-by-pher))
(def ^:private homing (juxt rank-by-home rank-by-pher))
(def ^:private turn-around #(turn % 4))

(defn ^:private rand-behavior [behavior place]
  (let [[ahead ahead-left ahead-right :as nearby] (world/nearby-places (:location place) (get-in place [:ant :dir]))
        ranks (apply merge-with + (behavior nearby))
        actions [#(move % ahead) #(turn % -1) #(turn % 1)]
        index (roulette [(if (:ant ahead) 0 (ranks ahead))
                         (ranks ahead-left)
                         (ranks ahead-right)])]
    ((actions index) place)))

(defn behave [place]
  (let [[ahead & _] (world/nearby-places (:location place) (get-in place [:ant :dir]))]
    (if (get-in place [:ant :food])
      (cond
        (world/home? place) (-> place drop-food turn-around)
        (and (world/home? ahead) (world/available? ahead)) (move place ahead)
        :else (rand-behavior homing place))
      (cond
        (and (world/food? place) (not (world/home? place))) (-> place take-food turn-around)
        (and (world/food? ahead) (not (world/home? ahead))) (move place ahead)
        :else (rand-behavior foraging place)))))

(def food? (comp boolean :food))
