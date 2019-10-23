(ns spbus.adapters.sptrans.parser-a-test
  (:require [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.adapters.sptrans.parser-a :as parser-a]
            [spbus.support.test-tools :as tools]))

(defn sample-row
  [fixture-file]
  (->> (spreadsheet/load-sheet fixture-file)
       (spreadsheet/rows)
       (drop 3)
       (first)
       (seq)))

(def row-type-a (sample-row "./test/fixtures/sptrans/type_a.xls"))
(def row-type-b (sample-row "./test/fixtures/sptrans/type_b.xls"))

(fact "parser-a/parseable? returns true only for a valid type a row"
  (parser-a/parseable? row-type-a) => true
  (parser-a/parseable? row-type-b) => false)

(fact "parser-a/line-id parses line ID"
  (parser-a/line-id row-type-a) => "N14311")

(fact "parser-a/pretty-line-id formats line ID"
  (parser-a/pretty-line-id row-type-a) => "N143-11")

(fact "parser-a/main-terminus parses main terminus"
  (parser-a/main-terminus row-type-a) => "METRO BARRA FUNDA")
