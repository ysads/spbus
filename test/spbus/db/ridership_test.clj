(ns spbus.db.ridership-test
  (:require [midje.sweet :refer :all]
            [spbus.db.ridership :as ridership]
            [spbus.protocols.storage-client :as client]
            [spbus.support.test-tools :as tools]))

(tools/init-system!)

(fact "Ridership DB repository has its own entity"
  ridership/entity => "ridership")
