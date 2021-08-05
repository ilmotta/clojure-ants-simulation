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

(defn roulette
  "Given a vector of slice sizes, returns the index of a slice given a random
  spin of a roulette wheel with compartments proportional to slices."
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (reduce
     (fn [{:keys [i slice-sum]} slice]
       (let [slice-sum (+ slice-sum slice)]
         (if (< r slice-sum)
           (reduced i)
           {:i (inc i)
            :slice-sum slice-sum})))
     {:i 0 :slice-sum 0}
     slices)))

(defn scaled-color ^long [{:keys [value max-value]}]
  (int (min 255 (* 255 (/ value max-value)))))

(defn delta [[ax ay bx by] [x y]]
  [(+ ax x) (+ ay y) (+ bx x) (+ by y)])

(defn scale [[h t] amount]
  [(* amount h) (* amount t)])
