(ns eu.cassiel.twizzle-test
  (:require [eu.cassiel.twizzle :as tw]
            [midje.sweet :refer :all]))

(fact "Unused channels return nil"
      (tw/sample (tw/initial) :WHATEVER) => nil)

(fact "Can sample an initial channel."
      (tw/sample (tw/initial :init {:X 42}) :X) => 42)

(fact "Start of fade"
      (-> (tw/initial :init {:X 99})
          (tw/automate :X 100 20 0)
          (tw/locate 100)
          (tw/sample :X))
      => (roughly 99))

(fact "Simple fade"
      (-> (tw/initial)
          (tw/automate :X 100 2 1.0)
          (tw/locate 101)
          (tw/sample :X))
      => (roughly 0.5))

(fact "Floating point location fade"
      (-> (tw/initial)
          (tw/automate :X 100 1 1.0)
          (tw/locate 100.5)
          (tw/sample :X))
      => (roughly 0.5))

(fact "End of fade"
      (-> (tw/initial :init {:X 99})
          (tw/automate :X 100 20 55)
          (tw/locate 120)
          (tw/sample :X))
      => (roughly 55))

(fact "Past end of fade"
      (-> (tw/initial :init {:X 99})
          (tw/automate :X 100 20 55)
          (tw/locate 999)
          (tw/sample :X))
      => (roughly 55))

(fact "No effect from future fade"
      (-> (tw/initial)
          (tw/automate :my-param 100 10 9.9)
          (tw/locate 50)
          (tw/sample :my-param))
      => nil)

(fact "Beyond end of fade"
      (-> (tw/initial)
          (tw/automate :my-param 100 10 9.9)
          (tw/locate 150)
          (tw/sample :my-param))
      => (roughly 9.9))

(fact "Purge"
      (-> (tw/initial)
          (tw/automate :my-param 100 10 9.9)
          (tw/locate 150)
          (tw/locate 50)
          (tw/sample :my-param))
      => (roughly 9.9))

(fact "Double locate within fade (1)"
      (-> (tw/initial)
          (tw/automate :my-param 100 10 10.0)
          (tw/locate 105)
          (tw/locate 107.5)
          (tw/sample :my-param))
      => (roughly 7.5))

(fact "Double locate within fade (2)"
      (-> (tw/initial)
          (tw/automate :my-param 100 10 10.0)
          (tw/locate 105)
          (tw/locate 102.5)
          (tw/sample :my-param))
      => (roughly 2.5))

(fact "Pre-inserted fade is chased"
      (-> (tw/initial)
          (tw/locate 100)
          (tw/automate :my-param 50 10 10.0)
          (tw/sample :my-param))
      => (roughly 10.0))

(fact "Pre-inserted fade is interpolated"
      (-> (tw/initial)
          (tw/locate 105)
          (tw/automate :my-param 100 10 10.0)
          (tw/sample :my-param))
      => (roughly 5.0))

(fact "Pre-inserted fades are chased"
      (-> (tw/initial)
          (tw/locate 100)
          (tw/automate :my-param 50 10 10.0)
          (tw/locate 55)
          (tw/sample :my-param))
      => (roughly 10.0))

(fact "Can handle fades with zero duration"
      (-> (tw/initial)
          (tw/automate :my-param 50 0 1.0)
          (tw/locate 100)
          (tw/sample :my-param))
      => (roughly 1.0))

(future-fact "Simple interpolation (use interpolation position as value"
             (-> (tw/initial :interp {:A (fn [start end pos] pos)})
                 (tw/automate :A 100 10 419)
                 (tw/locate 105)
                 (tw/sample :A))
             => (roughly 0.5))

(future-fact "Complex interpolation"
             (letfn [(interp [as bs pos]
                       (->> (interleave as bs)
                            (partition 2)
                            (map (fn [[x y]] (+ x (* pos (- y x)))))))]
               (-> (tw/initial :init {:A [0 1 0]}
                               :interp {:A interp})
                   (tw/automate :A 100 10 [1 0 0])
                   (tw/locate 105)
                   (tw/sample :A)))
             => (roughly [0.5 0.5 0]))
