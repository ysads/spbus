(ns spbus.db.ridership-test
  (:require [midje.sweet :refer :all]
            [monger.collection :as monger-document]
            [spbus.protocols.storage-client :as client]
            [spbus.db.ridership :as ridership]))

(def *storage* nil)
(def *db* nil)
(def test-config {:db-name "test"})
(def data {:pax 1000})

(fact "Ridership DB repository has its own entity"
  ridership/entity => "ridership")

;; (fact "about add-ridership"
;;   (fact "adds a new ridership to storage and return it"
;;     (ridership/add-ridership ..storage.. data) => {:_id 123 :pax 1000}
;;     (provided (client/update ..storage.. entity data) => {:_id 123 :pax 1000})))
