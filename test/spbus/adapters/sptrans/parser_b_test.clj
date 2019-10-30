(ns spbus.adapters.sptrans.parser-b-test
  (:require [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.adapters.sptrans.parser-b :as parser-b]
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

(fact "parser-b/parseable? returns true only for a valid type a row"
  (parser-b/parseable? row-type-a) => false
  (parser-b/parseable? row-type-b) => true)

(fact "parser-b/line-id parses line ID"
  (parser-b/line-id row-type-b) => "N10511")

(fact "parser-b/pretty-line-id formats line ID"
  (parser-b/pretty-line-id row-type-b) => "N105-11")

(fact "parser-b/main-terminus parses main terminus"
  (parser-b/main-terminus row-type-b) => "TERM CACHOEIRINHA")

(fact "parser-b/auxiliar-terminus parses main terminus"
  (parser-b/auxiliar-terminus row-type-b) => "TERM LAPA")

(fact "parser-b/company parses main terminus"
  (parser-b/company row-type-b) => "GATO PRETO")

(fact "paying pax totals are successfully parsed"
  (parser-b/paying-cash-pax row-type-b) => 64
  (parser-b/paying-normal-pax row-type-b) => 30
  (parser-b/paying-work-card-pax row-type-b) => 19
  (parser-b/paying-student-pax row-type-b) => 0
  (parser-b/paying-rail-bus-conn-pax row-type-b) => 12
  (parser-b/paying-month-pass-normal-pax row-type-b) => 6
  (parser-b/paying-month-pass-work-pax row-type-b) => 0
  (parser-b/paying-month-pass-student-pax row-type-b) => 0
  (parser-b/paying-month-pass-rail-bus-conn-pax row-type-b) => 0
  (parser-b/paying-pax row-type-b) => 131)

(fact "free pax totals are successfully parsed"
  (parser-b/free-bus-bus-conn-pax row-type-b) => 132
  (parser-b/free-normal-pax row-type-b) => 29
  (parser-b/free-student-pax row-type-b) => 13
  (parser-b/free-pax row-type-b) => 174)

(fact "total pax is parsed"
  (parser-b/total-pax row-type-b) => 305)

(fact "parser-b/parse returns a map with all information"
  (parser-b/parse row-type-b) => {:line-id "N10511"
                                  :line-code "N105"
                                  :branch-code "11"
                                  :main-terminus "TERM CACHOEIRINHA"
                                  :auxiliar-terminus "TERM LAPA"
                                  :company "GATO PRETO"
                                  :paying-pax {:cash 64
                                               :normal 30
                                               :work-card 19
                                               :student 0
                                               :rail-bus-connections 12
                                               :month-pass-normal 6
                                               :month-pass-work 0
                                               :month-pass-student 0
                                               :month-pass-rail-bus-connections 0
                                               :total 131}
                                  :free-pax {:bus-bus-connections 132
                                             :normal 29
                                             :student 13
                                             :total 174}
                                  :total-pax 305})
