(ns spbus.components.storage
  (:require [com.stuartsierra.component :as component]
            [failjure.core :as fail]
            [java-time :as time]
            [monger.core :as monger]
            [monger.collection :as monger-data]
            [spbus.protocols.storage-client :as storage-client])
  (:import org.bson.types.ObjectId java.lang.IllegalArgumentException)
  (:gen-class))

(defn ^:private db-name
  "The proper database name to which we will attach to. Note that component
  assumes a config map is available with the key :db-name holding this data.
  No fallback is assumed, thus in case this info is not available, process fails."
  [config]
  (if (:db-name config)
    (:db-name config)
    (throw (ex-info "Failed connecting to DB" {:cause ":db-name not given"}))))

(defn ^:private setup-db-conn
  [this]
  (let [conn (monger/connect)
        db (monger/get-db conn (db-name (:config this)))]
    (merge this {:conn conn :db db})))

(defn ^:private close-db-conn
  [this]
  (monger/disconnect (:conn this))
  (dissoc this :conn :db))

(defn ^:private object-id
  "Converts between string and ObjectId instances, always returning a valid
  object ID for this storage client."
  [id]
  (if (instance? ObjectId id)
    id
    (ObjectId. id)))

(defn ^:private insertion-ready-data
  "Prepares data to be inserted for the first time on DB."
  [data]
  (let [now (str (time/local-date-time))]
    (-> data
        (assoc :created-at now)
        (assoc :updated-at now)
        (assoc :_id (ObjectId.)))))

(defn ^:private update-ready-data
  "Prepares data to be updated on DB, which means touching the :update-at
  attribute to reflect the last time record was modified."
  [data]
  (let [now (str (time/local-date-time))]
    (assoc data :updated-at now)))

(defn ^:private find-one
  [db entity id]
  (let [oid (object-id id)]
    (monger-data/find-map-by-id db entity oid)))

(defn ^:private insert-one
  [db entity data]
  (monger-data/insert-and-return db entity (insertion-ready-data data)))

(defn ^:private find-many
  [db entity conditions]
  (monger-data/find-maps db entity conditions))

(defn ^:private update-one
  [db entity id data]
  (monger-data/find-and-modify db
                               entity
                               {:_id (object-id id)}
                               {:$set (update-ready-data data)}
                               {:return-new true}))

(defn ^:private delete-many
  [db entity conditions]
  (if (empty? conditions)
    (fail/fail "Can not delete without conditions")
    (-> (monger-data/remove db entity conditions)
        (.getN)
        (> 0))))

(defn ^:private delete-one
  [db entity id]
  (delete-many db entity {:_id (object-id id)}))

(defrecord MongoStorage [config conn db]
  component/Lifecycle
  (start [this]
    (setup-db-conn this))
  (stop [this]
    (close-db-conn this))

  storage-client/StorageClient
  (find-by-id [_this entity id]
    (find-one db entity id))
  (find [_this entity conditions]
    (find-many db entity conditions))
  (insert [_this entity data]
    (insert-one db entity data))
  (update-by-id [_this entity id updated-data]
    (update-one db entity id updated-data))
  (delete-by-id [_this entity id]
    (delete-one db entity id))
  (delete [_this entity conditions]
    (delete-many db entity conditions)))

(defn new-storage []
  (map->MongoStorage {}))
