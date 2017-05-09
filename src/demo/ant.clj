(ns demo.ant
  (:require [demo.util :refer [bound rank-by roulette]]
            [demo.world :as world]))

(defn drop-food [place]
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn trail [place]
  (update place :pher #(if (:home place) % (inc %))))

(defn move [from-place to-place]
  [(trail (dissoc from-place :ant))
   (assoc to-place :ant (:ant from-place))])

(defn take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn turn [place amount]
  (update-in place [:ant :dir] (comp (partial bound 8) +) amount))

(def rank-by-pher (partial rank-by :pher))
(def rank-by-home (partial rank-by #(if (:home %) 1 0)))
(def rank-by-food (partial rank-by :food))
(def foraging (juxt rank-by-food rank-by-pher))
(def homing (juxt rank-by-home rank-by-pher))
(def turn-around #(turn % 4))

(defn rand-behavior [config world behavior place]
  (let [[ahead ahead-left ahead-right :as nearby] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))
        ranks (apply merge-with + (behavior nearby))
        actions [#(move % ahead) #(turn % -1) #(turn % 1)]
        index (roulette [(if (:ant ahead) 0 (ranks ahead))
                         (ranks ahead-left)
                         (ranks ahead-right)])]
    ((actions index) place)))

(defn behave [config world place]
  (let [[ahead & _] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))]
    (if (get-in place [:ant :food])
      (cond
        (:home place) (-> place drop-food turn-around)
        (and (:home ahead) (not (:ant ahead))) (move place ahead)
        :else (rand-behavior config world homing place))
      (cond
        (and (pos? (:food place)) (not (:home place))) (-> place take-food turn-around)
        (and (pos? (:food ahead)) (not (:home ahead))) (move place ahead)
        :else (rand-behavior config world foraging place)))))
