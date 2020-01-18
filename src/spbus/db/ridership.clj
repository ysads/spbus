(ns spbus.db.ridership
  (:require [spbus.protocols.storage-client :as client])
  (:gen-class))

(def entity "ridership")

(defn ridership-with-id
  [storage doc-id]
  (client/find-by-id storage entity doc-id))

(defn riderships
  [storage conditions]
  (client/find storage entity conditions))

(defn add-ridership
  [storage doc]
  (client/insert storage entity doc))

(defn update-ridership
  [storage doc-id updated-document]
  (client/update storage entity doc-id updated-document))

(defn upsert-ridership
  [storage doc]
  (let [doc-id (:_id doc)
        doc-in-db (ridership-with-id storage doc-id)]
    (if (some? doc-in-db)
      (update-ridership storage doc-id doc)
      (add-ridership storage doc))))

(defn delete-ridership
  [storage doc-id]
  (client/delete storage entity {:_id doc-id}))
