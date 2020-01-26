(ns spbus.components-test
  (:require [spbus.components :as components]
            [midje.sweet :refer :all]))

(fact "components/system-for-env return proper systems according to env"
  (components/system-for-env :test) => (just {:config {:db-name "spbus-test"}
                                              :storage anything})
  (components/system-for-env :dev) => (just {:config {:db-name "spbus-dev"}
                                             :storage anything}))
