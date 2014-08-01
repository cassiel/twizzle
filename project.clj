(defproject eu.cassiel/twizzle "0.2.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles
  {:dev {:dependencies [[midje "1.6.3"]]
         :plugins [[lein-midje "3.1.3"]
                   [codox "0.8.10"]]
         :codox {:output-dir "../../cassiel.gh-pages/twizzle"
                 :defaults {:doc/format :markdown}
                 :src-dir-uri "http://github.com/cassiel/twizzle/blob/master/"
                 :src-linenum-anchor-prefix "L"}}})
