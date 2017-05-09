(ns demo.ant-test
  (:require [clojure.test :refer :all]
            [demo.ant :as ant]))

(deftest drop-food
  (let [place {:food 9 :ant {:food true :dir 10}}]
    (testing "drops food from ant"
      (is (= {:dir 10} (:ant (ant/drop-food place)))))
    (testing "increments places' food"
      (is (= {:food 10} (select-keys (ant/drop-food place) [:food]))))))

(deftest trail
  (testing "does not leave pheromone trail when at home"
    (is (= 8 (:pher (ant/trail {:home true :pher 8})))))
  (testing "leaves pheromone trail when not home"
    (is (= 9 (:pher (ant/trail {:home false :pher 8}))))))

(deftest move
  (testing "moves ant from initial place to destination"
    (is (= [nil {:dir 1}]
           (map :ant (ant/move {:pher 1 :ant {:dir 1}}
                               {:pher 2 :ant {:dir 2}})))))
  (testing "does not leave trail behind when at home"
    (is (= 8 (:pher (first (ant/move {:ant {:dir 1} :home true :pher 8} {:ant {:dir 2} :home true :pher 8})))))
    (is (= 8 (:pher (second (ant/move {:ant {:dir 1} :home true :pher 8} {:ant {:dir 2} :home true :pher 8}))))))
  (testing "leaves trail behind when not home"
    (is (= 9 (:pher (first (ant/move {:ant {:dir 1} :home false :pher 8} {:ant {:dir 2} :home false :pher 8})))))
    (is (= 8 (:pher (second (ant/move {:ant {:dir 1} :home false :pher 8} {:ant {:dir 2} :home false :pher 8})))))))

(deftest take-food
  (let [place {:food 1 :ant {:food false}}]
    (testing "decrements places' food"
      (is (= 0 (:food (ant/take-food place)))))
    (testing "marks ant with food"
      (is (= {:food true} (:ant (ant/take-food place)))))))
