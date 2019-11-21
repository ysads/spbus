(ns spbus.adapters.sptrans.parser-b-test
  (:require [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.adapters.sptrans.parser-b :as parser-b]
            [spbus.support.test-tools :as tools]))

(defn nth-row-in-sheet
  [sheet row-number]
  (-> (spreadsheet/nth-row sheet row-number :header-size 3)
      (seq)))

(def row-type-a (-> (spreadsheet/load-sheet "./test/fixtures/sptrans/type_a.xls")
                    (nth-row-in-sheet 0)))

(def type-a-sheet (spreadsheet/load-sheet "./test/fixtures/sptrans/type_b.xls"))
(def row-normal (nth-row-in-sheet type-a-sheet 0))
(def row-pre-boarding (nth-row-in-sheet type-a-sheet 1))
(def row-bad-format (nth-row-in-sheet type-a-sheet 2))
(def row-exp-tiradentes (nth-row-in-sheet type-a-sheet 3))

(fact "parser-b/parseable? returns true only for a valid type a row"
  (parser-b/parseable? row-type-a) => false
  (parser-b/parseable? row-pre-boarding) => true
  (parser-b/parseable? row-exp-tiradentes) => true
  (parser-b/parseable? row-normal) => true)

(fact "parser-b/line-id parses line ID"
  (parser-b/line-id row-bad-format) => "536210"
  (parser-b/line-id row-normal) => "N10511")

(fact "parser-b/pretty-line-id formats line ID"
  (parser-b/pretty-line-id row-normal) => "N105-11")

(fact "parser-b/main-terminus parses main terminus"
  (parser-b/main-terminus row-pre-boarding) => "TERM CIDADE TIRADENTES"
  (parser-b/main-terminus row-bad-format) => "PQ RES COCAIA"
  (parser-b/main-terminus row-normal) => "TERM CACHOEIRINHA")

(fact "parser-b/auxiliar-terminus parses main terminus"
		(parser-b/auxiliar-terminus row-pre-boarding) => "VIP"
  (parser-b/auxiliar-terminus row-bad-format) => "PÇA DA SÉ"
  (parser-b/auxiliar-terminus row-normal) => "TERM LAPA")

(fact "parser-b/company parses main terminus"
  (parser-b/company row-pre-boarding) => "VIP II"
  (parser-b/company row-bad-format) => "CIDADE DUTRA"
  (parser-b/company row-normal) => "GATO PRETO")

(fact "paying pax totals are successfully parsed"
  (parser-b/paying-cash-pax row-normal) => 64
  (parser-b/paying-normal-pax row-normal) => 30
  (parser-b/paying-work-card-pax row-normal) => 19
  (parser-b/paying-student-pax row-normal) => 0
  (parser-b/paying-rail-bus-conn-pax row-normal) => 12
  (parser-b/paying-month-pass-normal-pax row-normal) => 6
  (parser-b/paying-month-pass-work-pax row-normal) => 0
  (parser-b/paying-month-pass-student-pax row-normal) => 0
  (parser-b/paying-month-pass-rail-bus-conn-pax row-normal) => 0
  (parser-b/paying-pax row-normal) => 131)

(fact "free pax totals are successfully parsed"
  (parser-b/free-bus-bus-conn-pax row-normal) => 132
  (parser-b/free-normal-pax row-normal) => 29
  (parser-b/free-student-pax row-normal) => 13
  (parser-b/free-pax row-normal) => 174)

(fact "total pax is parsed"
  (parser-b/total-pax row-normal) => 305)

(fact "parser-b/parse returns a map with all information"
		(parser-b/parse row-pre-boarding) => (contains {:pre-boarding true})
  (parser-b/parse row-exp-tiradentes) => (contains {:pre-boarding true})
  (parser-b/parse row-bad-format) => (contains {:pre-boarding false
                                                :main-terminus "PQ RES COCAIA"
                                                :auxiliar-terminus "PÇA DA SÉ"
                                                :line-id "536210"})
  (parser-b/parse row-normal) => {:pre-boarding false
  																																:line-id "N10511"
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
