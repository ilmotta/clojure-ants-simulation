(ns demo.config)

(def ^:private dim 80)
(def ^:private nants-sqrt 7)
(def ^:private home-off (/ dim 4))
(def ^:private home-range (range home-off (+ nants-sqrt home-off)))

(def config
  {:animation-sleep-ms 100
   :ant-sleep-ms 40
   :dim 80 ; Dimensions of swaure world
   :evaporation-rate 0.99
   :evaporation-sleep-ms 1000
   :food-places 35 ; Number of places with food
   :food-range 100 ; Range of amount of food at a place
   :food-scale 30.0 ; Scale factor for food drawing
   :home-off home-off
   :home-range home-range
   :nants-sqrt nants-sqrt ; Number of ants = nants-sqrt^2
   :pher-scale 20.0 ; Scale factor for pheromone drawing
   :scale 5 ; Pixels per world cell
   })
