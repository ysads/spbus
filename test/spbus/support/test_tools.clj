(ns spbus.support.test-tools
  (:require [clojure.edn :as edn]
            [monger.collection :as monger-data]
            [monger.db :as monger-db]
            [spbus.system-utils :as system-utils]))

(def factories-path "./test/factories")
(def fixtures-path "./test/fixtures")

(defn ^:private fullpath
  ([path file extension]
   (str path "/" file extension))
  ([path file]
   (str path "/" file)))

(defn mock-from-factory
  [factory]
  (let [filename (fullpath factories-path factory ".edn")]
    (edn/read-string (slurp filename))))

(defn mock-from-fixture
  [fixture]
  (let [filename (fullpath fixtures-path fixture)]
    (slurp filename)))

(defn init-system! []
  (system-utils/running-system-for-env :test))
