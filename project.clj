(defproject eu.cassiel/twizzle "0.5.0"
  :description "A simple automation system for animation and realtime control"
  :url "https://github.com/cassiel/twizzle"
  :signing {:gpg-key "nick@cassiel.eu"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles
  {:dev {:dependencies [[midje "1.6.3"]]
         :plugins [[lein-midje "3.1.3"]
                   [michaelblume/lein-marginalia "0.9.0"]]}})
