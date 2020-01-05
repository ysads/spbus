(ns spbus.components.storage-test
  (:require [failjure.core :as fail]
            [java-time :as time]
            [midje.sweet :refer :all]
            [monger.collection :as monger-data]
            [monger.operators :refer :all]
            [spbus.components.storage :as storage]
            [spbus.protocols.storage-client :as client]))

(def *storage* (.start (storage/new-storage {})))
(def *db* (:db (:storage *storage*)))
(def test-entity "test")

(defn clean-db []
  (monger-data/remove *db* test-entity))

(with-state-changes [(before :facts (clean-db))]
  (facts "about put!"
    (fact "it persists data to mongoDB"
      (client/put! *storage* test-entity {:foo "bar"})
      (monger-data/count *db* test-entity {:foo "bar"}) => 1)

    (fact "it assocs :created-at to data being persisted"
      (let [now (time/local-date-time)]
        (client/put! *storage* test-entity {:foo "bar"}) => (contains {:created-at (str now)})
        (provided (time/local-date-time) => now)))

    (fact "it assocs :updated-at to data being persisted"
      (let [now (time/local-date-time)]
        (client/put! *storage* test-entity {:foo "bar"}) => (contains {:updated-at (str now)})
        (provided (time/local-date-time) => now))))

  (facts "about update!"
    (fact "it merges update data into persisted data"
      (let [data (client/put! *storage* test-entity {:foo 1 :bar 2})
            oid (:_id data)]
        (client/update! *storage* test-entity oid {:foo 0 :baz 3}) => (contains {:foo 0
                                                                                 :bar 2
                                                                                 :baz 3})))

    (fact "it touches :updated-at attribute of data being persisted"
      (let [data (client/put! *storage* test-entity {:foo "bar"})
            oid (:_id data)
            updated-data (client/update! *storage* test-entity oid {:foo 0})]
        (:created-at updated-data) => (:created-at data)
        (:updated-at updated-data) =not=> (:updated-at data))))

  (facts "about find-by-id"
    (fact "finds records using ObjectId instances"
      (let [data (client/put! *storage* test-entity {:foo "bar"})
            oid (:_id data)]
        (client/find-by-id *storage* test-entity oid) => (contains {:_id oid
                                                                    :foo "bar"})))

    (fact "finds records using string"
      (let [data (client/put! *storage* test-entity {:foo "bar"})
            oid (:_id data)
            string-id (.toString (:_id data))]
        (client/find-by-id *storage* test-entity string-id) => (contains {:_id oid
                                                                          :foo "bar"}))))

  (facts "about find"
    (fact "finds a collection of records that match given conditions"
      (client/put! *storage* test-entity {:foo 1})
      (client/put! *storage* test-entity {:bar 2})
      (client/put! *storage* test-entity {:baz 3})
      (client/find *storage* test-entity {$or [{:foo 1} {:bar 2}]}) => (just (contains {:foo 1})
                                                                             (contains {:bar 2}) :in-any-order)))

  (facts "about delete!"
    (fact "it does not allow deletion without conditions"
      (let [error (client/delete! *storage* test-entity {})]
        (fail/message error)=> "Can not delete without conditions"))

    (fact "removes from database records which match conditions"
      (client/put! *storage* test-entity {:foo 1})
      (client/put! *storage* test-entity {:bar 2})
      (client/put! *storage* test-entity {:baz 3})
      (client/delete! *storage* test-entity {$or [{:foo 1} {:bar 2}]})
      (monger-data/count *db* test-entity {:foo 1}) => 0
      (monger-data/count *db* test-entity {:bar 2}) => 0
      (monger-data/count *db* test-entity {:baz 3}) => 1)))
