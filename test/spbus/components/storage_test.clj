(ns spbus.components.storage-test
  (:require [com.stuartsierra.component :as component]
            [failjure.core :as fail]
            [java-time :as time]
            [midje.sweet :refer :all]
            [monger.collection :as monger-document]
            [monger.operators :refer :all]
            [spbus.components.storage :as storage]
            [spbus.protocols.storage-client :as client])
  (:import clojure.lang.ExceptionInfo
           org.bson.types.ObjectId))

(def ^:dynamic *storage* nil)
(def ^:dynamic *db* nil)
(def test-entity "test")
(def test-config {:db-name "test"})

(defn storage-with-config
  "A simple storage component, unstarted, but already mocked with a :config key.
  This allows us to have a config dependecy injected into storage and work around
  the limitation of not having a config component set here."
  [config-map]
  (assoc (storage/new-storage)
         :config
         config-map))

(defn setup-db []
  (alter-var-root #'*storage*
                  (fn [_] (component/start (storage-with-config test-config))))
  (alter-var-root #'*db*
                  (fn [_] (:db *storage*))))

(defn close-db []
  (alter-var-root #'*storage* #(component/stop %)))

(defn clean-db []
  (monger-document/remove *db* test-entity))

(facts "about db connection"
  (fact "it fails if no DB name is given at config"
    (component/start (storage-with-config {})) => (throws ExceptionInfo "Failed connecting to DB"))

  (fact "it connects to DB whose name is given as config argument"
    (let [db-config {:db-name "test-db"}
          storage (component/start (storage-with-config db-config))]
      (.getName (:db storage)) => "test-db")))

(with-state-changes [(before :contents (setup-db))
                     (before :facts (clean-db))
                     (after :contents (close-db))]
  (facts "about insert"
    (fact "it persists document to database"
      (client/insert *storage* test-entity {:foo "bar"})
      (monger-document/count *db* test-entity {:foo "bar"}) => 1)

    (fact "it assocs :created-at to document being persisted"
      (let [now (time/local-date-time)]
        (client/insert *storage* test-entity {:foo "bar"}) => (contains {:created-at (str now)})
        (provided (time/local-date-time) => now)))

    (fact "it assocs :updated-at to document being persisted"
      (let [now (time/local-date-time)]
        (client/insert *storage* test-entity {:foo "bar"}) => (contains {:updated-at (str now)})
        (provided (time/local-date-time) => now))))

  (facts "about insert-batch"
    (fact "it persists several documents at once"
      (client/insert-batch *storage* test-entity '({:test true}
                                                   {:test true}
                                                   {:test true}))
      (monger-document/count *db* test-entity {:test true}) => 3)

    (fact "it assocs :created-at to every document persisted"
      (client/insert-batch *storage* test-entity '({:test true}
                                                   {:test true}
                                                   {:test true}))
      (let [docs (monger-document/find-maps *db* test-entity {:test true})]
        (every? #(contains? % :created-at) docs) => true)))

  (facts "about update-by-id"
    (fact "it merges update document into persisted document"
      (let [document (client/insert *storage* test-entity {:foo 1 :bar 2})
            doc-id (:_id document)]
        (client/update-by-id *storage* test-entity doc-id {:foo 0 :baz 3}) => (contains {:foo 0
                                                                                         :bar 2
                                                                                         :baz 3})))

    (fact "it updates data even when :_id is given as string"
      (let [document (client/insert *storage* test-entity {:foo 1})
            doc-id (.toString (:_id document))]
        (client/update-by-id *storage* test-entity doc-id {:bar 2}) => (contains {:foo 1
                                                                                  :bar 2})))

    (fact "it touches :updated-at attribute of document being persisted"
      (let [document (client/insert *storage* test-entity {:foo "bar"})
            doc-id (:_id document)]
        ;; forces a time shift so that :updated-at might be updated
        (Thread/sleep 200)
        (client/update-by-id *storage* test-entity doc-id {:foo 0})
        (let [updated-document (client/find-by-id *storage* test-entity doc-id)]
          (:created-at updated-document) => (:created-at document)
          (:updated-at updated-document) =not=> (:updated-at document)))))

  (facts "about find-by-id"
    (fact "finds records using ObjectId instances"
      (let [document (client/insert *storage* test-entity {:foo "bar"})
            doc-id (:_id document)]
        (client/find-by-id *storage* test-entity doc-id) => (contains {:_id doc-id
                                                                       :foo "bar"})))

    (fact "finds records using string"
      (let [document (client/insert *storage* test-entity {:foo "bar"})
            doc-id (:_id document)
            string-id (.toString (:_id document))]
        (client/find-by-id *storage* test-entity string-id) => (contains {:_id doc-id
                                                                          :foo "bar"}))))

  (facts "about find"
    (fact "finds a collection of records that match given conditions"
      (client/insert *storage* test-entity {:foo 1})
      (client/insert *storage* test-entity {:bar 2})
      (client/insert *storage* test-entity {:baz 3})
      (client/find *storage* test-entity {$or [{:foo 1} {:bar 2}]}) => (just (contains {:foo 1})
                                                                             (contains {:bar 2}) :in-any-order)))

  (facts "about delete-by-id"
    (fact "removes single record from database if given ID matches any record"
      (let [doc (client/insert *storage* test-entity {:foo 1})]
        (client/delete-by-id *storage* test-entity (:_id doc)) => true))

    (fact "removes single record even if given ID is a string"
      (let [doc (client/insert *storage* test-entity {:foo 1})
            doc-id (.toString (:_id doc))]
        (client/delete-by-id *storage* test-entity doc-id) => true))

    (fact "is false if given string ID is not tied to a record on database"
      (let [random-id (ObjectId.)]
        (client/delete-by-id *storage* test-entity (ObjectId.)) => false)))

  (facts "about delete"
    (fact "it does not allow deletion without conditions"
      (let [error (client/delete *storage* test-entity {})]
        (fail/message error)=> "Can not delete without conditions"))

    (fact "removes from database records which match conditions"
      (client/insert *storage* test-entity {:foo 1})
      (client/insert *storage* test-entity {:bar 2})
      (client/insert *storage* test-entity {:baz 3})
      (client/delete *storage* test-entity {$or [{:foo 1} {:bar 2}]})
      (monger-document/count *db* test-entity {:foo 1}) => 0
      (monger-document/count *db* test-entity {:bar 2}) => 0
      (monger-document/count *db* test-entity {:baz 3}) => 1))

  (fact "returns whether any records were removed or not"
    (client/insert *storage* test-entity {:foo 1})
    (client/delete *storage* test-entity {:bar 2}) => false
    (client/delete *storage* test-entity {:foo 1}) => true))
