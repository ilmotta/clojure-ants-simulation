(ns demo.applet
  (:require [demo.ant :as ant]
            [demo.config :refer [ant-sleep-ms evaporation-sleep-ms
                                 animation-sleep-ms nants-sqrt home-off
                                 x-range y-range pher-scale scale dim food-scale]]
            [demo.store :as store]
            [demo.util :refer [scaled-color]]
            [demo.world :as world])
  (:import (javax.swing JApplet JPanel JFrame)
           (java.awt Color Graphics Dimension)
           (java.awt.image BufferedImage))
  (:gen-class :post-init post-init
              :extends javax.swing.JApplet))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ant sim ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(def x-scale (* scale dim))
(def y-scale (* scale dim))

(def directions
  {0 [2 0 2 4], 1 [4 0 0 4], 2 [4 2 0 2]
   3 [4 4 0 0], 4 [2 4 2 0], 5 [0 4 4 0]
   6 [0 2 4 2], 7 [0 0 4 4]})

(defn fill-cell [#^Graphics graphics x y color]
  (doto graphics
    (.setColor color)
    (.fillRect (* x scale) (* y scale) scale scale)))

(defn ant-color [ant]
  (if (ant/food? ant) (Color/red) (Color/black)))

(defn food-color [food]
  (new Color 255 0 0 (scaled-color food food-scale)))

(defn pheromone-color [pheromone]
  (new Color 0 255 0 (scaled-color pheromone pher-scale)))

(defn render-ant [ant #^Graphics graphics x y]
  (let [[hx hy tx ty] (directions (:dir ant))
        x-scale (* x scale)
        y-scale (* y scale)]
    (doto graphics
      (.setColor (ant-color ant))
      (.drawLine (+ hx x-scale) (+ hy y-scale) (+ tx x-scale) (+ ty y-scale)))))

(defn render-all-places [img]
  (let [places (dosync (store/place))
        graphics (.getGraphics img)]
    (dorun
      (for [x x-range y y-range]
        (let [{:keys [pher food ant] :as place} (places (+ (* x dim) y))]
          (when (world/pheromone? place) (fill-cell graphics x y (pheromone-color pher)))
          (when (world/food? place) (fill-cell graphics x y (food-color food)))
          (when (world/ant? place) (render-ant ant graphics x y)))))))

(defn fill-world-bg [img]
  (doto (.getGraphics img)
    (.setColor (Color/white))
    (.fillRect 0 0 (.getWidth img) (.getHeight img))))

(defn render-home [img]
  (doto (.getGraphics img)
    (.setColor (Color/blue))
    (.drawRect (* scale home-off) (* scale home-off)
               (* scale nants-sqrt) (* scale nants-sqrt))))

(defn render [g]
  (let [img (BufferedImage. x-scale y-scale (BufferedImage/TYPE_INT_ARGB))
        bg (.getGraphics img)]
    (doto img (fill-world-bg) (render-all-places) (render-home))
    (.drawImage g img 0 0 nil)
    (.dispose bg)))

(def panel
  (doto (proxy [JPanel] [] (paint [g] (render g)))
    (.setPreferredSize (new Dimension x-scale y-scale))))

(def animator (agent nil))
(def evaporator (agent nil))

(defn animation-loop [_]
  (send-off *agent* animation-loop)
  (.repaint panel)
  (Thread/sleep animation-sleep-ms)
  nil)

(defn evaporation-loop [_]
  (send-off *agent* evaporation-loop)
  (dosync (-> (store/place) (map world/evaporate) store/update-place))
  (Thread/sleep evaporation-sleep-ms)
  nil)

(defn ant-loop [location]
  (Thread/sleep ant-sleep-ms)
  (dosync
    (send-off *agent* ant-loop)
    (-> location store/place ant/behave store/update-place :location)))

(defn start-ants []
  (dorun
    (for [place (->> (dosync (store/place)) (filter :ant))]
      (send-off (get-in place [:ant :agent]) ant-loop))))

(defn -post-init [this]
  (doto this (.setContentPane panel) (.setVisible true))
  (send-off animator animation-loop)
  (send-off evaporator evaporation-loop)
  (dosync (world/setup))
  (start-ants))
