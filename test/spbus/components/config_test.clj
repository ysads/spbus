(ns spbus.components.config-test
  (:require [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]
            [spbus.components.config :as config]))

(fact "config/base-config contains bootstrap configs"
  config/base-config => {:db-name "spbus"})

(fact "config/new-config merges given configs with base-configs"
  (config/new-config {:db-port 5432}) => (contains {:db-name "spbus"
                                                    :db-port 5432})
  (config/new-config {:db-name "test"}) => (contains {:db-name "test"}))

(facts "about Config component"
  (fact "starting component doesn't change configs stored"
    (-> (config/new-config {})
        (component/start)) => (contains {:db-name "spbus"}))

  (fact "stoping component doesn't change configs stored"
    (-> (config/new-config {})
        (component/start)
        (component/stop)) => (contains {:db-name "spbus"})))
