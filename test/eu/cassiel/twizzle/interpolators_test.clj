(ns eu.cassiel.twizzle.interpolators-test
  (:require [eu.cassiel.twizzle.interpolators :as it]
            [midje.sweet :refer :all]))

(facts "Vectors"
       (fact "Vector interp"
             (it/interp-vectors (fn [x y p] (+ x (* p (- y x))))
                                [0 1 2] [5 4 10] 1/2)
             => [5/2 5/2 6])

       (fact "Vector const"
             (it/interp-vectors (fn [_ _ p] p)
                                [0 1 2] [5 4 10] 1/3)
             => (repeat 3 1/3))

       (fact "Vector interp from nil"
             (it/interp-vectors it/interp-default
                                nil
                                [5 4 10]
                                1/2)
             => [5/2 4/2 10/2]))
