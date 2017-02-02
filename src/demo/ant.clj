(ns demo.ant
  (:require [demo.config :refer [config]]
            [demo.world :as world]
            [demo.util :refer [bound rank-by wrand]]))

(defstruct ant :dir :agent) ; May also have :food

(defn ^:private next-direction [amount direction]
  (bound 8 (+ direction amount)))

(defn ^:private drop-pheromone [place]
  (update place :pher #(if (:home place) % (inc %))))

(defn ^:private move
  [from-place to-place]
  [(-> from-place (dissoc :ant) drop-pheromone)
   (assoc to-place :ant (:ant from-place))])

(defn ^:private turn [place amount]
  (update-in place [:ant :dir] next-direction amount))

(defn ^:private take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn ^:private drop-food [place]
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn food? [ant]
  (boolean (:food ant)))

(defn build-ant [place]
  (struct ant (rand-int 8) (agent (world/cell (:location place)))))

(defn behave-loop
  "the main function for the ant agent"
  [cell]
  (let [ant (:ant @cell)
        [ahead ahead-left ahead-right] (world/close-cells (:location @cell) (:dir ant))
        cells [ahead ahead-left ahead-right]]
    (. Thread (sleep (config :ant-sleep-ms)))
    (dosync
      (send-off *agent* behave-loop)
      (if (:food ant)
        ;going home
        (cond
          (:home @cell)
          (-> @cell drop-food (turn 4) world/update-place)
          (and (:home @ahead) (not (:ant @ahead)))
          (-> @cell (move @ahead) world/update-place)
          :else
          (let [ranks (merge-with + (rank-by (comp #(if (:home %) 1 0) deref) cells) (rank-by (comp :pher deref) cells))
                index (wrand [(if (:ant @ahead) 0 (ranks ahead)) (ranks ahead-left) (ranks ahead-right)])
                actions [#(move % @ahead) #(turn % -1) #(turn % 1)]]
            (-> @cell ((actions index)) world/update-place)))
        ;foraging
        (cond
          (and (pos? (:food @cell)) (not (:home @cell)))
          (-> @cell take-food (turn 4) world/update-place)
          (and (pos? (:food @ahead)) (not (:home @ahead)) (not (:ant @ahead)))
          (world/update-place (move @cell @ahead))
          :else
          (let [ranks (merge-with + (rank-by (comp :food deref) cells) (rank-by (comp :pher deref) cells))
                index (wrand [(if (:ant @ahead) 0 (ranks ahead)) (ranks ahead-left) (ranks ahead-right)])
                actions [#(move % @ahead) #(turn % -1) #(turn % 1)]]
            (-> @cell ((actions index)) world/update-place)))))))
