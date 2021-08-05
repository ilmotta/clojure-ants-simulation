(ns demo.domain)

(defrecord Ant [dir agent food])
(defrecord Cell [ant food home location pher])

(defn build-cell [args]
  (map->Cell (merge {:food 0 :pher 0 :home false :location [0 0]} args)))

(defn build-ant [args]
  (map->Ant args))
