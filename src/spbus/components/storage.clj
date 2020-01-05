(ns spbus.components.storage
  (:require [com.stuartsierra.component :as component]
            [failjure.core :as fail]
            [java-time :as time]
            [monger.core :as monger]
            [monger.collection :as monger-data]
            [spbus.protocols.storage-client :as storage-client])
  (:import org.bson.types.ObjectId)
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

(defn ^:private object-id
  "Converts between string and ObjectId instances, always returning a valid
  object ID for this storage client."
  [id]
  (if (instance? ObjectId id)
    id
    (ObjectId. id)))

(defn ^:private insertion-prepared-data
  "Prepares data to be inserted for the first time on DB."
  [data]
  (let [now (str (time/local-date-time))]
    (-> data
        (assoc :created-at now)
        (assoc :updated-at now)
        (assoc :_id (ObjectId.)))))

(defn ^:private find-one!
  [db entity id]
  (let [oid (object-id id)]
    (monger-data/find-map-by-id db entity oid)))

(defn ^:private insert-data!
  [db entity data]
  (monger-data/insert-and-return db entity (insertion-prepared-data data)))

(defn ^:private find-collection!
  [db entity conditions]
  (monger-data/find-maps db entity conditions))

(defn ^:private update-data!
  [db entity id data]
  (let [updated-data (assoc data :updated-at (str (time/local-date-time)))]
    (monger-data/find-and-modify db
                                entity
                                {:_id id}
                                {:$set updated-data}
                                {:return-new true})))

(defn ^:private delete-collection!
  [db entity conditions]
  (if (empty? conditions)
    (fail/fail "Can not delete without conditions")
    (monger-data/remove db entity conditions)))

(defrecord MongoStorage [storage]
  component/Lifecycle
  (start [this]
    (setup-db-conn this))
  (stop [this]
    (close-db-conn this))

  storage-client/StorageClient
  (find-by-id [_this entity id]
    (find-one! (:db storage) entity id))
  (find [_this entity conditions]
    (find-collection! (:db storage) entity conditions))
  (put! [_this entity data]
    (insert-data! (:db storage) entity data))
  (update! [_this entity id updated-data]
    (update-data! (:db storage) entity id updated-data))
  (delete! [_this entity conditions]
    (delete-collection! (:db storage) entity conditions)))

(defn new-storage
  [system]
  (map->MongoStorage system))
