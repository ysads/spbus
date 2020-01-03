(ns spbus.components.config
  (:require [com.stuartsierra.component :as component])
  (:gen-class))

(def base-config {:db-name "spbus"})

(defrecord Config [config]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn new-config
  [config-map]
  (map->Config {:config (merge base-config config-map)}))
