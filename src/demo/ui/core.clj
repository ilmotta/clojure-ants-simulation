(ns demo.ui.core
  (:require [demo.util :as util])
  (:import (javax.swing JApplet JFrame JPanel)
           (java.awt Graphics Color Dimension)
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

(defn color ^Color [[^long r ^long g ^long b] value max-value]
  (new Color r g b (util/scaled-color {:value value :max-value max-value})))

(defn make-img ^BufferedImage [[^long x ^long y]]
  (BufferedImage. x y (BufferedImage/TYPE_INT_ARGB)))

(defn make-frame ^JFrame [^JApplet applet ^String name]
  (doto (JFrame. name)
    (.add (.getContentPane applet))
    (.pack)
    (.setLocationByPlatform true)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn render [^Graphics graphics width height on-render]
  (let [img (make-img [width height])]
    (on-render img)
    (.drawImage graphics img 0 0 nil)
    (.dispose (.getGraphics img))))

(defn make-panel [width height on-render]
  (doto (proxy [JPanel] []
          (paint [graphics] (render graphics width height on-render)))
    (.setPreferredSize (new Dimension width height))))

(defn make-rect [^BufferedImage img {:keys [color border fill]}]
  (let [graphics (.getGraphics img)]
    (when color
      (.setColor graphics (colors color)))
    (when border
      (.drawRect graphics (nth border 0) (nth border 1) (nth border 2) (nth border 3)))
    (when fill
      (.fillRect graphics (nth fill 0) (nth fill 1) (nth fill 2) (nth fill 3)))))

(defn make-line [^BufferedImage img {:keys [color border]}]
  (let [graphics (.getGraphics img)]
    (when color
      (.setColor graphics (colors color)))
    (when border
      (.drawLine graphics (nth border 0) (nth border 1) (nth border 2) (nth border 3)))))

(defn make-applet ^JApplet [panel]
  (doto (new JApplet)
    (.setContentPane panel)
    (.setVisible true)))
