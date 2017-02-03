(ns demo.core
  (:require [demo.applet])
  (:import (javax.swing JApplet JPanel JLabel JFrame))
  (:gen-class :post-init post-init
              :main -main))

(defn -main []
  (let [applet (new demo.applet)]
    (doto (JFrame. "Ants")
      (.add (.getContentPane applet))
      (.pack)
      (.setLocationByPlatform true)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))))
