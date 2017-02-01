(ns demo.util)

(defn rank-by
  "Returns a map of xs to their 1-based rank when sorted by keyfn."
  [keyfn xs]
  (let [sorted (sort-by (comp float keyfn) xs)]
    (reduce (fn [ret i] (assoc ret (nth sorted i) (inc i)))
            {} (range (count sorted)))))

(defn bound
  "Returns n wrapped into range 0-b"
  [b n]
  (let [n (rem n b)]
    (if (neg? n)
      (+ n b)
      n)))

(defn wrand
  "Given a vector of slice sizes, returns the index of a slice given a random
  spin of a roulette wheel with compartments proportional to slices."
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (loop [i 0 sum 0]
      (if (< r (+ (slices i) sum))
        i
        (recur (inc i) (+ (slices i) sum))))))

(defn scaled-color [value max-value]
  (int (min 255 (* 255 (/ value max-value)))))
