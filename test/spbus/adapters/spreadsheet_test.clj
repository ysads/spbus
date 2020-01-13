(ns spbus.adapters.spreadsheet-test
  (:require [clojure.spec.alpha :as spec]
            [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.support.test-tools :as tools]))

(def sheet-1 (spreadsheet/load-sheet "./test/fixtures/sptrans/type_a.xls"))
(def sheet-2 (spreadsheet/load-sheet "./test/fixtures/sptrans/type_b.xls"))

(fact "spreadsheet/load-sheet opens a workbook and return its first sheet"
  (class sheet-1) => org.apache.poi.hssf.usermodel.HSSFSheet
  (class sheet-2) => org.apache.poi.hssf.usermodel.HSSFSheet)

(fact "spreadsheet/rows converts sheet data into seq"
  (seq? (spreadsheet/rows sheet-1)) => true
  (seq? (spreadsheet/rows sheet-2)) => true)

(fact "spreadsheet/rows accepts a header-size key which discards the n first lines"
  (let [sheet-1-rows (spreadsheet/rows sheet-1)
        sheet-1-rows-with-header (spreadsheet/rows sheet-1 :header-size 3)]
    (count sheet-1-rows-with-header) => (- (count sheet-1-rows) 3)
    (seq? sheet-1-rows-with-header) => true))

(def rows-sheet-1 (spreadsheet/rows sheet-1))

(fact "spreadsheet/nth-row gets the proper row from the sheet"
  (spreadsheet/nth-row sheet-1 0) => (nth rows-sheet-1 0)
  (spreadsheet/nth-row sheet-1 1) => (nth rows-sheet-1 1)
  (spreadsheet/nth-row sheet-1 0 :header-size 1) => (nth rows-sheet-1 1)
  (spreadsheet/nth-row sheet-1 4 :header-size 2) => (nth rows-sheet-1 6))

(defn sample-cell
  [sheet cell-number]
  (-> (spreadsheet/rows sheet)
      (as-> all-rows (drop 3 all-rows))
      (first)
      (seq)
      (nth cell-number)))

(def cell-4 (sample-cell sheet-1 4))
(def cell-5 (sample-cell sheet-1 5))

(fact "spreadsheet/cell-value extracts the value of a given cell"
  (spreadsheet/cell-value cell-4) => "N14311 - METRO BARRA FUNDA/MORRO GRANDE"
  (spreadsheet/cell-value cell-5) => 3.0)
