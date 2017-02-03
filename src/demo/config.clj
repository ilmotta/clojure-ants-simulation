(ns demo.config)

(def animation-sleep-ms 100)
(def ant-sleep-ms 40)
(def dim 80) ; World dimensions
(def evaporation-rate 0.99)
(def evaporation-sleep-ms 1000)
(def food-places 35) ; Number of places with food
(def food-range 100) ; Max amount of food
(def food-scale 30.0) ; Scale factor for food drawing
(def food-scale 30.0) ; Scale factor for food drawing
(def nants-sqrt 7) ; Number of ants = nants-sqrt^2
(def pher-scale 20.0) ; Scale factor for pheromone drawing
(def scale 5) ; Pixels per world cell

(def home-off (/ dim 4))
(def home-range (range home-off (+ nants-sqrt home-off)))
(def x-range (range dim))
(def y-range (range dim))
