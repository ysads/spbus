(ns spbus.db.ridership
  (:require [monger.operators :refer :all]
            [spbus.protocols.storage-client :as client]
            [clojure.string :as string])
  (:gen-class))

(def entity "ridership")
(def text-search-keys [:line-id :line-code :branch-code
                       :main-terminus :auxiliar-terminus
                       :company :stop-name :stop-id])

(defn ^:private regex-match-field
  [field terms]
  {field {$regex (str ".*" (string/upper-case terms) ".*")}})

(defn ridership-with-id
  [storage doc-id]
  (client/find-by-id storage entity doc-id))

(defn riderships
  [storage conditions]
  (client/find storage entity conditions))

(defn text-search
  [storage terms]
  (let [query (map #(regex-match-field % terms) text-search-keys)]
    (client/find storage entity {$or query})))

(defn add-ridership
  [storage doc]
  (client/insert storage entity doc))

(defn update-ridership
  [storage doc-id updated-document]
  (client/update-by-id storage entity doc-id updated-document))

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
