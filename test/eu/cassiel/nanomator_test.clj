(ns eu.cassiel.nanomator-test
  (:require [eu.cassiel.nanomator :as nn]
            [midje.sweet :refer :all]))

(fact "Unused channels return nil"
      (nn/sample (nn/initial) :WHATEVER) => nil)

(fact "Can sample an initial channel."
      (nn/sample (nn/initial :init {:X 42}) :X) => 42)

(fact "Start of fade"
      (-> (nn/initial :init {:X 99})
          (nn/automate :X 100 20 0)
          (nn/locate 100)
          (nn/sample :X))
      => (roughly 99))

(fact "Simple fade"
      (-> (nn/initial)
          (nn/automate :X 100 2 1.0)
          (nn/locate 101)
          (nn/sample :X))
      => (roughly 0.5))

(fact "End of fade"
      (-> (nn/initial :init {:X 99})
          (nn/automate :X 100 20 55)
          (nn/locate 120)
          (nn/sample :X))
      => (roughly 55))

(fact "Past end of fade"
      (-> (nn/initial :init {:X 99})
          (nn/automate :X 100 20 55)
          (nn/locate 999)
          (nn/sample :X))
      => (roughly 55))

(fact "safe purge"
      (-> (nn/initial)
          (nn/automate :my-param 100 10 9.9)
          (nn/locate 50)
          (nn/sample :my-param))
      => nil)

(fact "partial purge"
      (-> (nn/initial)
          (nn/automate :my-param 100 10 10.0)
          (nn/locate 105)
          (nn/sample :my-param))
      => (roughly 5.0))

(fact "locate purges (1)"
      (-> (nn/initial)
          (nn/automate :my-param 100 10 9.9)
          (nn/locate 150)
          (nn/sample :my-param))
      => (roughly 9.9))

(fact "locate purges (2)"
      (-> (nn/initial)
          (nn/automate :my-param 100 10 9.9)
          (nn/locate 150)
          (nn/locate 50)
          (nn/sample :my-param))
      => (roughly 9.9))

(fact "pre-insert fade"
      (-> (nn/initial)
          (nn/locate 100)
          (nn/automate :my-param 50 10 10.0)
          (nn/sample :my-param))
      => (roughly 10.0))

(fact "pre-insert fade 2"
      (-> (nn/initial)
          (nn/locate 105)
          (nn/automate :my-param 100 10 10.0)
          (nn/sample :my-param))
      => (roughly 5.0))

(fact "can handle fades with zero duration"
      (-> (nn/initial)
          (nn/automate :my-param 50 0 1.0)
          (nn/locate 100)
          (nn/sample :my-param)))

(future-fact "Simple interpolation (use interpolation position as value"
             (-> (nn/initial :interp {:A (fn [start end pos] pos)})
                 (nn/automate :A 100 10 419)
                 (nn/locate 105)
                 (nn/sample :A))
             => (roughly 0.5))

(future-fact "Complex interpolation"
             (letfn [(interp [as bs pos]
                       (->> (interleave as bs)
                            (partition 2)
                            (map (fn [[x y]] (+ x (* pos (- y x)))))))]
               (-> (nn/initial :init {:A [0 1 0]}
                               :interp {:A interp})
                   (nn/automate :A 100 10 [1 0 0])
                   (nn/locate 105)
                   (nn/sample :A)))
             => (roughly [0.5 0.5 0]))
