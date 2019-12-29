(defproject spbus "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clojure.java-time "0.3.2"]
                 [com.stuartsierra/component "0.4.0"]
                 [dk.ative/docjure "1.12.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [reaver "0.1.2"]]
  :repl-options {:init-ns spbus.core}
  :profiles {:dev {:dependencies [[midje "1.9.8"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-cloverage "1.0.9"]]}
             :midje {}})
