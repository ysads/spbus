(ns spbus.system
  (:require [com.stuartsierra.component :as component]
            [spbus.components.config :as config]
            [spbus.components.storage :as storage])
  (:gen-class))

(defn test-system []
  (component/system-map
   :config (config/new-config {:db-name "spbus-test"})
   :storage (component/using (storage/new-storage) [:config])))

(defn dev-system []
  (component/system-map
   :config (config/new-config {:db-name "spbus-dev"})
   :storage (component/using (storage/new-storage) [:config])))

(def all-systems {:dev dev-system
                  :test test-system})


