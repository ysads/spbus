(ns spbus.components.storage-test
  (:require [spbus.components.storage :as storage]
            [spbus.protocols.storage-client :as client]
            [monger.collection :as monger-data]
            [midje.sweet :refer :all]))

(def *storage* (.start (storage/new-storage {})))
(def *db* (:db (:storage *storage*)))
(def test-entity "test")

(defn clean-db []
  (monger-data/remove *db* test-entity))

(with-state-changes [(before :facts (clean-db))]
  (facts "about put!"
    (fact "it persists data to mongoDB"
      (client/put! *storage* test-entity {:foo "bar"})
      (monger-data/count *db* test-entity {:foo "bar"}) => 1))

  (facts "about find-by-id"
    (fact "finds records using ObjectId instances"
      (let [oid (client/put! *storage* test-entity {:foo "bar"})]
        (client/find-by-id *storage* test-entity oid) => {:_id oid
                                                          :foo "bar"}))

    (fact "finds records using string"
      (let [oid (client/put! *storage* test-entity {:foo "bar"})
            string-id (.toString oid)]
        (client/find-by-id *storage* test-entity string-id) => {:_id oid
                                                                :foo "bar"}))))
