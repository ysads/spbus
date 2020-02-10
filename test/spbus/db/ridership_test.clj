(ns spbus.db.ridership-test
  (:require [midje.sweet :refer :all]
            [monger.collection :as monger-data]
            [spbus.db.ridership :as ridership]
            [spbus.protocols.storage-client :as client]
            [spbus.support.test-tools :as tools]
            [spbus.system-utils :as system-utils]))

(def system nil)
(def storage nil)

(def ridership-ent ridership/entity)

(defn setup-system []
  (let [current-system (tools/init-system!)]
    (alter-var-root #'system (constantly current-system))
    (alter-var-root #'storage (constantly (:storage current-system)))
    (monger-data/remove (:db storage) ridership/entity)))

(defn stop-system []
  (system-utils/stop-system))

(def bus-normal (tools/mock-from-factory "ridership/bus-normal"))

(with-state-changes [(before :contents (setup-system))
                     (after :contents (stop-system))]
  (fact "Ridership DB repository has its own entity"
    ridership/entity => "ridership")

  (fact "about add-ridership"
    (ridership/add-ridership storage bus-normal)
    (client/find storage ridership-ent {}) => (just (contains bus-normal)))

  (fact "ridership-with-id finds a ridership based on its ID"
    (let [ridership (ridership/add-ridership storage bus-normal)
          ridership-id (:_id ridership)]
      (ridership/ridership-with-id storage ridership-id) => (contains bus-normal)))

  (fact "ridership/search-with-text can match records based on its text-searcheable keys"
    (ridership/add-ridership storage {:company "FOOBAT"})
    (ridership/add-ridership storage {:stop-name "FOO"})
    (ridership/add-ridership storage {:line-code "FOO BAR"})
    (ridership/add-ridership storage {:main-terminus "BAR"})
    (ridership/text-search storage "foo") => (just (contains {:company "FOOBAT"})
                                                   (contains {:stop-name "FOO"})
                                                   (contains {:line-code "FOO BAR"}))))
