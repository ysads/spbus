(ns spbus.components.config-test
  (:require [midje.sweet :refer :all]
            [spbus.components.config :as config]))

(fact "config/base-config contains bootstrap configs"
  config/base-config => {:db-name "spbus"})
