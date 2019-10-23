(ns spbus.adapters.sptrans.parser-a
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [spbus.adapters.spreadsheet :as spreadsheet])
  (:gen-class))

(defn parseable?
  [row]
  (= 16 (count row)))


(defn ^:private line-code
  "Returns the line main code. Examples:
   - 803210 => 8032;
   - N81211 => N812."
  [id]
  (.substring (java.lang.String. id) 0 (- (count id) 2)))

(defn ^:private branch-code
  "Returns the line main code. Examples:
   - 803210 => 10;
   - N81211 => 11."
  [id]
  (.substring (java.lang.String. id)
              (- (count id) 2)
              (count id)))

(defn line-id
  [row]
  (let [content (spreadsheet/cell-value row 4)]
    (first (str/split content #" - "))))

(defn pretty-line-id
  [row]
  (let [raw-id (line-id row)]
    (str (line-code raw-id) "-" (branch-code raw-id))))

(defn main-terminus
  [row]
  (let [content (spreadsheet/cell-value row 4)]
    (-> (str/split content #" - ")
        (last)
        (str/split #"/")
        (first))))
