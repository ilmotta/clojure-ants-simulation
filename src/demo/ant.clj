(ns demo.ant
  (:require [demo.config :refer [config]]
            [demo.store :as store]
            [demo.util :refer [bound rank-by wrand]]
            [demo.world :as world]))

(defstruct ant :dir :agent) ; May also have :food

(def ^:private rank-by-pher (partial rank-by :pher))
(def ^:private rank-by-home (partial rank-by #(if (:home %) 1 0)))
(def ^:private rank-by-food (partial rank-by :food))

(defn ^:private next-direction [amount direction]
  (bound 8 (+ direction amount)))

(defn ^:private drop-pheromone [place]
  (update place :pher #(if (:home place) % (inc %))))

(defn ^:private move [from-place to-place]
  [(-> from-place (dissoc :ant) drop-pheromone)
   (assoc to-place :ant (:ant from-place))])

(defn ^:private turn [place amount]
  (update-in place [:ant :dir] next-direction amount))

(defn ^:private take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn ^:private drop-food [place]
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn ^:private rand-behavior [[ahead ahead-left ahead-right :as nearby-places] ranks]
  (let [final-ranks (apply merge-with + ranks)
        index (wrand [(if (:ant ahead) 0 (final-ranks ahead)) (final-ranks ahead-left) (final-ranks ahead-right)])
        actions [#(move % ahead) #(turn % -1) #(turn % 1)]]
    (actions index)))

(defn ^:private behave [place]
  (let [places (world/nearby-places (:location place) (get-in place [:ant :dir]))
        [ahead ahead-left ahead-right] places]
    (if (get-in place [:ant :food])
      (cond
        (:home place) (-> place drop-food (turn 4))
        (world/home-and-available? ahead) (move place ahead)
        :else ((rand-behavior places ((juxt rank-by-home rank-by-pher) places)) place))
      (cond
        (world/has-food-and-not-home? place) (-> place take-food (turn 4))
        (world/has-food-and-not-home? ahead) (move place ahead)
        :else ((rand-behavior places ((juxt rank-by-food rank-by-pher) places)) place)))))

(defn behave-loop [location]
  (Thread/sleep (config :ant-sleep-ms))
  (dosync
    (send-off *agent* behave-loop)
    (-> location store/place behave store/update-place :location)))

(defn food? [ant]
  (boolean (:food ant)))

(defn build [place]
  (struct ant (rand-int 8) (agent (:location place))))
