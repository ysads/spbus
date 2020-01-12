(ns spbus.protocols.storage-client
  (:gen-class))

(defprotocol StorageClient
  "A protocol which defines the contract between storage adapters
  and the application."
  (find-by-id [storage entity id]
    "Returns a single object based on its unique identifier.")
  (find [storage entity conditions]
    "Returns a collection of objects based on given conditions.")
  (insert [storage entity data]
    "Insert a new object to database under given entity.")
  (update [storage entity id updated-data]
    "Merges updated-data into the data already persisted to database at given ID.")
  (delete [storage entity conditions]
    "Removes a record from database."))
