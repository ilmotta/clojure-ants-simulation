(ns demo.ui.ant
  (:require [demo.ant :as ant]
            [demo.config :as config]
            [demo.ui.core :as ui]))

(def ^:private directions
  {0 [2 0 2 4], 1 [4 0 0 4], 2 [4 2 0 2]
   3 [4 4 0 0], 4 [2 4 2 0], 5 [0 4 4 0]
   6 [0 2 4 2], 7 [0 0 4 4]})

(defn ^:private ant-color [ant]
  (if (ant/food? ant) :red :black))

(defn ^:private delta [[ax ay bx by] [x y]]
  [(+ ax x) (+ ay y) (+ bx x) (+ by y)])

(defn ^:private scale [[h t] amount]
  [(* amount h) (* amount t)])

(defn ^:private next-loc [dir loc]
  (-> dir directions (delta (scale loc config/scale))))

(defn render-ant [ant img x y]
  (ui/make-line img {:color (ant-color ant)
                     :border (next-loc (:dir ant) [x y])}))
