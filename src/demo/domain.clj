(ns demo.domain)

(defrecord Ant
  [dir agent food])

(defrecord Cell
  [ant food home location pher])

(defn build-cell [args]
  (map->Cell args))

(defn build-ant [args]
  (map->Ant args))
