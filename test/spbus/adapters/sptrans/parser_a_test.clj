(ns spbus.adapters.sptrans.parser-a-test
  (:require [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.adapters.sptrans.parser-a :as parser-a]
            [spbus.support.test-tools :as tools]))

(defn nth-row-in-sheet
  [sheet row-number]
  (-> (spreadsheet/nth-row sheet row-number :header-size 3)
      (seq)))

(def row-type-b (-> (spreadsheet/load-sheet "./test/fixtures/sptrans/type_b.xls")
                    (nth-row-in-sheet 0)))

(def type-a-sheet (spreadsheet/load-sheet "./test/fixtures/sptrans/type_a.xls"))
(def row-normal (nth-row-in-sheet type-a-sheet 0))
(def row-pre-boarding (nth-row-in-sheet type-a-sheet 1))

(fact "parser-a/parseable? returns true only for a valid type a row"
  (parser-a/parseable? row-type-b) => false
  (parser-a/parseable? row-normal) => true
  (parser-a/parseable? row-pre-boarding) => true)

(fact "parser-a/pre-boarding? returns true if the row doesnt represent a line"
  (parser-a/pre-boarding? row-normal) => false
  (parser-a/pre-boarding? row-pre-boarding) => true)

(fact "parser-a/line-id parses line ID"
  (parser-a/line-id row-normal) => "N14311")

(fact "parser-a/pretty-line-id formats line ID"
  (parser-a/pretty-line-id row-normal) => "N143-11")

(fact "parser-a/main-terminus parses main terminus"
  (parser-a/main-terminus row-normal) => "METRO BARRA FUNDA")

(fact "parser-a/auxiliar-terminus parses main terminus"
  (parser-a/auxiliar-terminus row-normal) => "MORRO GRANDE")

(fact "parser-a/company parses main terminus"
  (parser-a/company row-normal) => "202")

(fact "paying pax totals are successfully parsed"
  (parser-a/paying-cash-pax row-normal) => 3
  (parser-a/paying-normal-work-card-pax row-normal) => 35
  (parser-a/paying-student-pax row-normal) => 1
  (parser-a/paying-month-pass-student-pax row-normal) => 0
  (parser-a/paying-month-pass-work-pax row-normal) => 0
  (parser-a/paying-month-pass-normal-pax row-normal) => 4
  (parser-a/paying-pax row-normal) => 43)

(fact "free pax totals are successfully parsed"
  (parser-a/free-bus-bus-conn-pax row-normal) => 18
  (parser-a/free-normal-pax row-normal) => 7
  (parser-a/free-student-pax row-normal) => 1
  (parser-a/free-pax row-normal) => 26)

(fact "total pax is parsed"
  (parser-a/total-pax row-normal) => 69)

(fact "parser-a/parse returns a map with all information"
  (parser-a/parse row-normal) => {:pre-boarding false
                                  :line-id "N14311"
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
