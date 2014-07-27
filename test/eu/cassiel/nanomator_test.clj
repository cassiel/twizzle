(ns eu.cassiel.nanomator-test
  (:require [eu.cassiel.nanomator :as nn]
            [midje.sweet :refer :all]))

(fact "Unused channels return 0"
      (nn/sample (nn/initial) 0 :WHATEVER) => 0)

(fact "Can sample an initial channel."
      (nn/sample (nn/initial :init {:X 42}) :X 0) => 42)

(fact "Start of fade"
      (as-> (nn/initial :init {:X 99}) S
            (nn/automate S :X 100 20 0)
            (nn/sample S :X 100))
      => (roughly 99))

(fact "Simple fade"
      (as-> (nn/initial) S
            (nn/automate S :X 100 2 1.0)
            (nn/sample S :X 101))
      => (roughly 0.5))

(fact "End of fade"
      (as-> (nn/initial :init {:X 99}) S
            (nn/automate S :X 100 20 55)
            (nn/sample S :X 120))
      => (roughly 55))

(fact "Past end of fade"
      (as-> (nn/initial :init {:X 99}) S
            (nn/automate S :X 100 20 55)
            (nn/sample S :X 999))
      => (roughly 55))
