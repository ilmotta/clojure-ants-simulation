(ns demo.ui.world
  (:require [demo.ui.ant :as ui-ant]
            [demo.world :as world]
            [demo.ui.core :as ui]
            [demo.util :as util]))

(defn food-color [config food]
  (ui/color [255 0 0] food (:food-scale config)))

(defn pheromone-color [config pheromone]
  (ui/color [0 255 0] pheromone (:pher-scale config)))

(defn render-place-as-pheromone [img config pher x y]
  (ui/make-rect img {:color (pheromone-color config pher)
                     :fill [(* x (:scale config))
                            (* y (:scale config))
                            (:scale config) (:scale config)]}))

(defn render-place-as-food [img config food x y]
  (ui/make-rect img {:color (food-color config food)
                     :fill [(* x (:scale config))
                            (* y (:scale config))
                            (:scale config) (:scale config)]}))

(defn render-home [img {:keys [scale home-off nants-sqrt]}]
  (ui/make-rect img
                {:color :blue
                 :border [(* scale home-off) (* scale home-off)
                          (* scale nants-sqrt) (* scale nants-sqrt)]}))

(defn fill-world-bg [img]
  (ui/make-rect img {:color :white
                     :fill [0 0 (.getWidth img) (.getHeight img)]}))

(defn render-all-places [img config world]
  (doseq [x (range (:dim config)), y (range (:dim config))]
    (let [{:keys [pher food ant]} (-> world (get-in [x y]) deref)]
      (when (pos? pher) (render-place-as-pheromone img config pher x y))
      (when (pos? food) (render-place-as-food img config food x y))
      (when ant (ui-ant/render-ant ant img config x y)))))
