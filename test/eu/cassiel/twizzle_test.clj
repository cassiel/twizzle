(ns eu.cassiel.twizzle
  (:require [eu.cassiel.twizzle :as tt]
            [midje.sweet :refer :all]))

(fact "Unused chattels return nil"
      (tt/sample (nn/initial) :WHATEVER) => nil)

(fact "Can sample an initial channel."
      (tt/sample (tt/initial :init {:X 42}) :X) => 42)

(fact "Start of fade"
      (-> (tt/initial :init {:X 99})
          (tt/automate :X 100 20 0)
          (tt/locate 100)
          (tt/sample :X))
      => (roughly 99))

(fact "Simple fade"
      (-> (tt/initial)
          (tt/automate :X 100 2 1.0)
          (tt/locate 101)
          (tt/sample :X))
      => (roughly 0.5))

(fact "End of fade"
      (-> (tt/initial :init {:X 99})
          (tt/automate :X 100 20 55)
          (tt/locate 120)
          (tt/sample :X))
      => (roughly 55))

(fact "Past end of fade"
      (-> (tt/initial :init {:X 99})
          (tt/automate :X 100 20 55)
          (tt/locate 999)
          (tt/sample :X))
      => (roughly 55))

(fact "safe purge"
      (-> (tt/initial)
          (tt/automate :my-param 100 10 9.9)
          (tt/locate 50)
          (tt/sample :my-param))
      => nil)

(fact "partial purge"
      (-> (tt/initial)
          (tt/automate :my-param 100 10 10.0)
          (tt/locate 105)
          (tt/sample :my-param))
      => (roughly 5.0))

(fact "locate purges (1)"
      (-> (tt/initial)
          (tt/automate :my-param 100 10 9.9)
          (tt/locate 150)
          (tt/sample :my-param))
      => (roughly 9.9))

(fact "locate purges (2)"
      (-> (tt/initial)
          (tt/automate :my-param 100 10 9.9)
          (tt/locate 150)
          (tt/locate 50)
          (tt/sample :my-param))
      => (roughly 9.9))

(fact "pre-insert fade"
      (-> (tt/initial)
          (tt/locate 100)
          (tt/automate :my-param 50 10 10.0)
          (tt/sample :my-param))
      => (roughly 10.0))

(fact "pre-insert fade 2"
      (-> (tt/initial)
          (tt/locate 105)
          (tt/automate :my-param 100 10 10.0)
          (tt/sample :my-param))
      => (roughly 5.0))

(fact "can handle fades with zero duration"
      (-> (tt/initial)
          (tt/automate :my-param 50 0 1.0)
          (tt/locate 100)
          (tt/sample :my-param)))

(future-fact "Simple interpolation (use interpolation position as value"
             (-> (tt/initial :interp {:A (fn [start end pos] pos)})
                 (tt/automate :A 100 10 419)
                 (tt/locate 105)
                 (tt/sample :A))
             => (roughly 0.5))

(future-fact "Complex interpolation"
             (letfn [(interp [as bs pos]
                       (->> (interleave as bs)
                            (partition 2)
                            (map (fn [[x y]] (+ x (* pos (- y x)))))))]
               (-> (tt/initial :init {:A [0 1 0]}
                               :interp {:A interp})
                   (tt/automate :A 100 10 [1 0 0])
                   (tt/locate 105)
                   (tt/sample :A)))
             => (roughly [0.5 0.5 0]))
