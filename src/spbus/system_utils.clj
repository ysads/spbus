(ns spbus.system-utils
  (:require [com.stuartsierra.component :as component]
            [spbus.components :as spbus-components])
  (:gen-class))

(def current-system (atom nil))

(defn stop-system []
  (swap! current-system #(component/stop %)))

(defn create-and-start-system
  [env]
  (->> (spbus-components/system-for-env env)
       (component/start)
       (reset! current-system)))

(defn running-system-for-env
  [env]
  (or (deref current-system)
      (create-and-start-system env)))
