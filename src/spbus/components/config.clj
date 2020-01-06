(ns spbus.components.config
  (:require [com.stuartsierra.component :as component])
  (:gen-class))

(def base-config {:db-name "spbus"})

(defrecord Config []
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn new-config
  [config-map]
  (map->Config (merge base-config config-map)))
