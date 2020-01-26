(ns spbus.system-utils-test
  (:require [spbus.system-utils :as system-utils]
            [midje.sweet :refer :all]))

(defn clear-system []
  (reset! system-utils/current-system nil))

(with-state-changes [(before :facts (clear-system))]
  (facts "about system-utils/running-system-for-env"
    (fact "bootstraps a new system if none is running"
      @system-utils/current-system => nil
      (system-utils/running-system-for-env :test)
      @system-utils/current-system => (just {:config anything
                                             :storage anything}))))
