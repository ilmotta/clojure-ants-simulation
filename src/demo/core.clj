(ns demo.core
  (:require [demo.ui.core :as ui]
            [demo.ui.world :as ui-world]
            [demo.config :as config]
            [demo.world :as world]
            [demo.ant :as ant]
            [demo.store :as store])
  (:gen-class :main -main))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ant sim ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(def x-scale (* config/scale config/dim))
(def y-scale (* config/scale config/dim))

(def animator (agent nil))
(def evaporator (agent nil))

(defn animation-loop [_ panel]
  (send-off *agent* animation-loop panel)
  (.repaint panel)
  (Thread/sleep config/animation-sleep-ms)
  nil)

(defn evaporation-loop [_]
  (send-off *agent* evaporation-loop)
  (dosync (-> (store/place) (map world/evaporate) store/update-place))
  (Thread/sleep config/evaporation-sleep-ms)
  nil)

(defn ant-loop [location]
  (Thread/sleep config/ant-sleep-ms)
  (dosync
    (send-off *agent* ant-loop)
    (-> location store/place ant/behave store/update-place :location)))

(defn start-ants []
  (dorun
    (for [place (->> (dosync (store/place)) (filter :ant))]
      (send-off (get-in place [:ant :agent]) ant-loop))))

(defn start [panel]
  (send-off animator animation-loop panel)
  (send-off evaporator evaporation-loop)
  (dosync (world/setup))
  (start-ants))

(defn on-render [img]
  (doto img
    (ui-world/fill-world-bg)
    (ui-world/render-all-places (dosync (store/place)))
    (ui-world/render-home {:scale config/scale :home-off config/home-off :nants-sqrt config/nants-sqrt})))

(defn -main []
  (let [panel (ui/make-panel x-scale y-scale on-render)]
    (-> (ui/make-applet panel)
        (ui/make-frame "Ants"))
    (start panel)))
