(ns spbus.components.storage-test
  (:require [spbus.components.storage :as storage]
            [spbus.protocols.storage-client :as client]
            [monger.collection :as monger-data]
            [midje.sweet :refer :all]))

(def *storage* (.start (storage/new-storage {})))
(def *db* (:db (:storage *storage*)))
(def test-entity "test")

(fact "storage allows insertion using put!"
  (client/put! *storage* test-entity {:foo "bar"})
  *db* => 1
  (monger-data/count *db* test-entity {:foo "bar"}) => 1)
