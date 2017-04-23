(ns demo.ui.world
  (:require [demo.config :as config]
            [demo.ui.ant :as ui-ant]
            [demo.world :as world]
            [demo.ui.core :as ui]
            [demo.util :as util]))

(defn ^:private food-color [food]
  (ui/color [255 0 0] food config/food-scale))

(defn ^:private pheromone-color [pheromone]
  (ui/color [0 255 0] pheromone config/pher-scale))

(defn ^:private render-place-as-pheromone [img pher x y]
  (ui/make-rect img {:color (pheromone-color pher)
                     :fill [(* x config/scale) (* y config/scale) config/scale config/scale]}))

(defn ^:private render-place-as-food [img food x y]
  (ui/make-rect img {:color (food-color food)
                     :fill [(* x config/scale) (* y config/scale) config/scale config/scale]}))

(defn render-home [img {:keys [scale home-off nants-sqrt]}]
  (ui/make-rect img
                {:color :blue
                 :border [(* scale home-off) (* scale home-off)
                          (* scale nants-sqrt) (* scale nants-sqrt)]}))

(defn fill-world-bg [img]
  (ui/make-rect img {:color :white
                     :fill [0 0 (.getWidth img) (.getHeight img)]}))

(defn render-all-places [img places]
  (dorun
    (for [x config/x-range y config/y-range]
      (let [{:keys [pher food ant] :as place} (places (+ (* x config/dim) y))]
        (when (world/pheromone? place) (render-place-as-pheromone img pher x y))
        (when (world/food? place) (render-place-as-food img food x y))
        (when (world/ant? place) (ui-ant/render-ant ant img x y))))))
