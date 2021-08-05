(ns demo.core
  (:require [demo.ant :as ant]
            [demo.domain :as domain]
            [demo.ui.core :as ui]
            [demo.ui.world :as ui-world]
            [demo.world :as world])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ant sim ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(defonce animator (agent nil))
(defonce evaporator (agent nil))

(defonce app-state
  (atom {:panel nil
         :applet nil
         :frame nil
         :world nil
         :config {:animation-sleep-ms 100
                  :ant-sleep-ms 40
                  :dim 120 ; World dimensions
                  :evaporation-rate 0.99
                  :evaporation-sleep-ms 1000
                  :food-places 100 ; Number of places with food
                  :food-range 100 ; Max amount of food
                  :food-scale 30.0 ; Scale factor for food drawing
                  :nants-sqrt 10 ; Number of ants = nants-sqrt^2
                  :pher-scale 20.0 ; Scale factor for pheromone drawing
                  :scale 5 ; Pixels per world cell
                  }}))

(defn on-render [state img]
  (doto img
    (ui-world/fill-world-bg)
    (ui-world/render-all-places (:config state) (:world state))
    (ui-world/render-home {:scale (get-in state [:config :scale])
                           :home-off (/ (get-in state [:config :dim]) 4)
                           :nants-sqrt (get-in state [:config :nants-sqrt])})))

(defn init-panel [state]
  (swap! state
         (fn [state]
           (let [x-scale (* (get-in state [:config :scale]) (get-in state [:config :dim]))
                 y-scale x-scale]
             (assoc state :panel (ui/make-panel x-scale y-scale #(on-render state %)))))))

(defn init-applet [state]
  (swap! state
         (fn [state]
           (assoc state :applet (ui/make-applet (:panel state))))))

(defn init-frame [state]
  (swap! state
         (fn [state]
           (assoc state :frame (ui/make-frame (:applet state) "Ants")))))

;; World is a 2D vector of refs to cells.
(defn init-world [state]
  (swap! state
         (fn [state]
           (assoc state :world
                  (mapv (fn [x] (mapv (fn [y] (ref (domain/build-cell {:location [x y]})))
                                      (range (get-in state [:config :dim]))))
                        (range (get-in state [:config :dim]))))))
  state)

(defn init-home [state]
  (let [home-off (/ (get-in @state [:config :dim]) 4)
        home-range (range home-off (+ (get-in @state [:config :nants-sqrt]) home-off))]
    (doseq [x home-range y home-range
            :let [place (get-in (:world @state) [x y])]]
      (alter place assoc :home true
             :ant (domain/build-ant {:dir (rand-int 8)
                                     :agent (agent (:location @place))}))))
  state)

(defn init-ants [state]
  (doseq [row (:world @state), col row]
    (alter col assoc :ant
           (domain/build-ant {:dir (rand-int 8)
                              :agent (agent (:location col))})))
  state)

(defn init-food [state]
  (doseq [_ (range (get-in @state [:config :food-places]))
          :let [random-loc [(rand-int (get-in @state [:config :dim]))
                            (rand-int (get-in @state [:config :dim]))]]]
    (-> @state
        (:world)
        (get-in random-loc)
        (alter assoc :food (rand-int (get-in @state [:config :food-range])))))
  state)

(defn reset-food [state]
  (doseq [row (:world @state), col row]
    (alter col assoc :food 0))
  (init-food state))

(defn reset-pheromones [state]
  (doseq [row (:world @state), col row]
    (alter col assoc :pher 0)))

(defn animation-loop [_ state]
  (send-off *agent* animation-loop state)
  (.repaint (:panel @state))
  (Thread/sleep (get-in @state [:config :animation-sleep-ms]))
  nil)

(defn evaporation-loop [_ state]
  (send-off *agent* evaporation-loop state)
  (dosync
   (doseq [row (:world @state), col row]
     (alter col update :pher * (get-in @state [:config :evaporation-rate]))))
  (Thread/sleep (get-in @state [:config :evaporation-sleep-ms]))
  nil)

(defn ant-loop [location state]
  (Thread/sleep (get-in @state [:config :ant-sleep-ms]))
  (dosync
   (send-off *agent* ant-loop state)
   (let [world (:world @state)
         place @(get-in world location)
         new-places (flatten [(ant/behave (:config @state) world place)])]
     (doseq [new-place new-places]
       (ref-set (get-in world (:location new-place)) new-place))
     (-> new-places last :location))))

(defn start-ants [state]
  (doseq [row (:world @state), col row
          :let [place @col]
          :when (:ant place)]
    (send-off (get-in place [:ant :agent]) ant-loop state)))

(defn start [state]
  (send-off animator animation-loop state)
  (send-off evaporator evaporation-loop state)
  (start-ants state))

(defn -main []
  (dosync (-> app-state
              (init-world)
              (init-home)
              (init-food)))
  (doto app-state
    (init-panel)
    (init-applet)
    (init-frame)
    (start)))

(comment
  (-main))
