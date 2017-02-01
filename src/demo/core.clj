(ns demo.core
  (:import (javax.swing JApplet JPanel JLabel JFrame))
  (:gen-class :post-init post-init
              :main -main))

(compile 'demo.applet)

(defn -main []
  (let [applet (new demo.applet)]
    (doto (JFrame. "Ants")
      (.add (.getContentPane applet))
      (.pack)
      (.setLocationByPlatform true)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))))
