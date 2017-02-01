(ns demo.config)

(def config
  {:animation-sleep-ms 100
   :ant-sleep-ms 40
   :dim 80 ; Dimensions of swaure world
   :evaporation-rate 0.99
   :evaporation-sleep-ms 1000
   :food-places 35 ; Number of places with food
   :food-range 100 ; Range of amount of food at a place
   :food-scale 30.0 ; Scale factor for food drawing
   :nants-sqrt 7 ; Number of ants = nants-sqrt^2
   :pher-scale 20.0 ; Scale factor for pheromone drawing
   :scale 5 ; Pixels per world cell
   })
