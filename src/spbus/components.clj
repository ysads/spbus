(ns spbus.components
  (:require [com.stuartsierra.component :as component]
            [spbus.components.config :as config]
            [spbus.components.storage :as storage])
  (:gen-class))

(def test-system
  (component/system-map
   :config (config/new-config {:db-name "spbus-test"})
   :storage (component/using (storage/new-storage) [:config])))

(def dev-system
  (component/system-map
   :config (config/new-config {:db-name "spbus-dev"})
   :storage (component/using (storage/new-storage) [:config])))

(def all-systems {:dev dev-system
                  :test test-system})

(defn system-for-env
  [env]
  (get all-systems env))
