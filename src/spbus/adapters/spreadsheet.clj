(ns spbus.adapters.spreadsheet
  (:require [clojure.java.io :as io]
            [dk.ative.docjure.spreadsheet :as sheet])
  (:gen-class))

(defn load-sheet
  "Downloads spreadsheet from a particular link and load its first
  working sheet into memory."
  [url]
  (with-open [stream (io/input-stream url)]
    (-> (sheet/load-workbook stream)
        (sheet/sheet-seq)
        (first))))

(defn rows
  "Returns a seq to allow iteration over sheet data"
  [sheet & {:keys [header-size]}]
  (let [lines-to-drop (or header-size 0)]
    (->> (sheet/row-seq sheet)
         (drop lines-to-drop))))

(defn nth-row
  "Returns the nth row of a sheet. It accepts an optional header-size param
  which states how many rows should not be taken into account when finding
  the nth row"
  [sheet row-number & {:keys [header-size]}]
  (-> (rows sheet :header-size header-size)
      (nth row-number)))

(defn cell-value
  "Returns the cell value in its proper type"
  ([cell]
   (sheet/read-cell cell))
  ([row column]
   (cell-value (nth row column))))
