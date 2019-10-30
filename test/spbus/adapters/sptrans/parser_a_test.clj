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

(fact "parser-a/auxiliar-terminus parses main terminus"
  (parser-a/auxiliar-terminus row-type-a) => "MORRO GRANDE")

(fact "parser-a/company parses main terminus"
  (parser-a/company row-type-a) => "202")

(fact "paying pax totals are successfully parsed"
  (parser-a/paying-cash-pax row-type-a) => 3
  (parser-a/paying-normal-work-card-pax row-type-a) => 35
  (parser-a/paying-student-pax row-type-a) => 1
  (parser-a/paying-month-pass-student-pax row-type-a) => 0
  (parser-a/paying-month-pass-work-pax row-type-a) => 0
  (parser-a/paying-month-pass-normal-pax row-type-a) => 4
  (parser-a/paying-pax row-type-a) => 43)

(fact "free pax totals are successfully parsed"
  (parser-a/free-bus-bus-conn-pax row-type-a) => 18
  (parser-a/free-normal-pax row-type-a) => 7
  (parser-a/free-student-pax row-type-a) => 1
  (parser-a/free-pax row-type-a) => 26)

(fact "total pax is parsed"
  (parser-a/total-pax row-type-a) => 69)

(fact "parser-a/parse returns a map with all information"
  (parser-a/parse row-type-a) => {:line-id "N14311"
                                  :line-code "N143"
                                  :branch-code "11"
                                  :main-terminus "METRO BARRA FUNDA"
                                  :auxiliar-terminus "MORRO GRANDE"
                                  :company "202"
                                  :paying-pax {:cash 3
                                               :normal-and-work-card 35
                                               :student 1
                                               :month-pass-normal 4
                                               :month-pass-work 0
                                               :month-pass-student 0
                                               :total 43}
                                  :free-pax {:bus-bus-connections 18
                                             :normal 7
                                             :student 1
                                             :total 26}
                                  :total-pax 69})
