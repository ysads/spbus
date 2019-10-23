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
