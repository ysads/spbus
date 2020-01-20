(ns spbus.components
  (:require [spbus.components.storage :as storage]
            [spbus.components.config :as config])
  (:gen-class))


(def current-system (atom {}))

(def create-new-system-for
  [env]
  )

(defn running-system-for
  [env]
  (or (deref (current-system))
      (create-new-system-for env)))
