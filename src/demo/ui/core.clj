(ns demo.ui.core
  (:require [demo.util :as util])
  (:import (javax.swing JApplet JFrame JPanel)
           (java.awt Color Dimension)
           (java.awt.image BufferedImage)))

(def all-colors
  {:blue (Color/blue)
   :red (Color/red)
   :black (Color/black)
   :white (Color/white)})

(defn colors [color]
  (if (instance? Color color)
    color
    (all-colors color)))

(defn color [[r g b] value max-value]
  (new Color r g b (util/scaled-color {:value value :max-value max-value})))

(defn make-img [[x y]]
  (BufferedImage. x y (BufferedImage/TYPE_INT_ARGB)))

(defn make-frame [applet name]
  (doto (JFrame. name)
    (.add (.getContentPane applet))
    (.pack)
    (.setLocationByPlatform true)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn render [graphics width height on-render]
  (let [img (make-img [width height])]
    (on-render img)
    (.drawImage graphics img 0 0 nil)
    (.dispose (.getGraphics img))))

(defn make-panel [width height on-render]
  (doto (proxy [JPanel] []
          (paint [graphics] (render graphics width height on-render)))
    (.setPreferredSize (new Dimension width height))))

(defn make-rect [img opts]
  (let [graphics (.getGraphics img)]
    (when-let [color (:color opts)]
      (.setColor graphics (colors color)))
    (when-let [border (:border opts)]
      (.drawRect graphics (nth border 0) (nth border 1) (nth border 2) (nth border 3)))
    (when-let [fill (:fill opts)]
      (.fillRect graphics (nth fill 0) (nth fill 1) (nth fill 2) (nth fill 3)))))

(defn make-line [img opts]
  (let [graphics (.getGraphics img)]
    (when-let [color (:color opts)]
      (.setColor graphics (colors color)))
    (when-let [border (:border opts)]
      (.drawLine graphics (nth border 0) (nth border 1) (nth border 2) (nth border 3)))))

(defn make-applet [panel]
  (doto (new JApplet)
    (.setContentPane panel)
    (.setVisible true)))
