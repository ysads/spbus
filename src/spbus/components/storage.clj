(ns spbus.components.storage
  (:require [com.stuartsierra.component :as component]
            [monger.core :as monger])
  (:import [com.mongodb MongoOptions ServerAddress])
  (:gen-class))

(defn ^:private setup-db-conn
  [this]
  (let [conn (monger/connect)
        db (monger/get-db conn "spbus")]
    (merge this {:storage {:conn conn :db db}}))) ;; db name should come from a config

(defn ^:private close-db-conn
  [this]
  (monger/disconnect (:conn this))
  (update-in this [:storage] dissoc :conn :storage))

(defrecord MongoStorage [storage]
  component/Lifecycle
  (start [this]
    (setup-db-conn this))
  (stop [this]
    (close-db-conn this)))

(defn new-storage
  [system]
  (map->MongoStorage system))