(ns demo.world
  (:require [demo.util :as util]))

(def direction-delta
  {0 [0 -1]
   1 [1 -1]
   2 [1 0]
   3 [1 1]
   4 [0 1]
   5 [-1 1]
   6 [-1 0]
   7 [-1 -1]})

(defn delta-location [config location direction]
  (->> direction
       (util/bound 8)
       (direction-delta)
       (map + location)
       (map (partial util/bound (:dim config)))))

(defn nearby-places [config world location direction]
  (->> direction
       ((juxt identity dec inc))
       (map (partial delta-location config location))
       (map (partial get-in world))
       (map deref)))
