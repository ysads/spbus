(ns spbus.components
  (:require [com.stuartsierra.component :as component]
            [spbus.components.config :as config]
            [spbus.components.storage :as storage])
  (:gen-class))

(def base-system
  (component/system-map
   :config (config/new-config {:db-name "spbus-dev"})
   :storage (component/using (storage/new-storage) [:config])))

(def test-system
  (merge base-system
         {:config (config/new-config {:db-name "spbus-test"})}))

(def all-systems {:dev base-system
                  :test test-system})

(defn system-for-env
  [env]
  (get all-systems env))
