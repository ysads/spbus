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
  [sheet]
  (sheet/row-seq sheet))

(defn nth-row
  [sheet row-number & {:keys [header-size]}]
  (let [sheet-rows (rows sheet)]
    (if header-size
      (-> (drop header-size sheet-rows)
          (nth row-number))
      (nth sheet-rows row-number))))

(defn cell-value
  "Returns the cell value in its proper type"
  ([cell]
   (sheet/read-cell cell))
  ([row column]
   (cell-value (nth row column))))
