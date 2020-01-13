(ns spbus.adapters.sptrans-test
  (:require [clojure.spec.alpha :as spec]
            [java-time :as time]
            [midje.sweet :refer :all]
            [spbus.adapters.spreadsheet :as spreadsheet]
            [spbus.adapters.sptrans :as sptrans]
            [spbus.adapters.sptrans.parser-a :as parser-a]
            [spbus.adapters.sptrans.parser-b :as parser-b]
            [spbus.support.test-tools :as tools])
  (:import clojure.lang.ExceptionInfo))

(fact "sptrans/statistics-url is the correct URL"
  sptrans/statistics-url => "https://www.prefeitura.sp.gov.br/cidade/secretarias/transportes/institucional/sptrans/acesso_a_informacao/agenda/index.php?p=269652")

(fact "sptrans/month converts a humanized month name to integer"
  (sptrans/month "Janeiro") => "01"
  (sptrans/month "Fevereiro") => "02"
  (sptrans/month "Março") => "03"
  (sptrans/month "Abril") => "04"
  (sptrans/month "Maio") => "05"
  (sptrans/month "Junho") => "06"
  (sptrans/month "Julho") => "07"
  (sptrans/month "Agosto") => "08"
  (sptrans/month "Setembro") => "09"
  (sptrans/month "Outubro") => "10"
  (sptrans/month "Novembro") => "11"
  (sptrans/month "Dezembro") => "12")

(def raw-link-05 (tools/mock-from-factory "sptrans/raw_link_05"))
(def raw-link-30 (tools/mock-from-factory "sptrans/raw_link_30"))
(def raw-link-total (tools/mock-from-factory "sptrans/raw_link_total"))

(fact "sptrans/month-total? returns true when raw link contains a mention to that"
  (sptrans/month-total? raw-link-05) => false
  (sptrans/month-total? raw-link-30) => false
  (sptrans/month-total? raw-link-total) => true)

(def date-format "yyyy-MM-dd")

(fact "sptrans/link-date conjoins the date of a given link"
  (sptrans/link-date raw-link-05 "Janeiro")  => (time/local-date date-format "2019-01-05")
  (sptrans/link-date raw-link-30 "Setembro") => (time/local-date date-format "2019-09-30")
  (sptrans/link-date raw-link-total "Março") => nil)

(fact "sptrans/link-url returns the correct URL"
  (sptrans/link-url raw-link-05) => "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/05012019.xls"
  (sptrans/link-url raw-link-30) => "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/30SET2019.xls"
  (sptrans/link-url raw-link-total) => "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/Consolidado Julho-2019.xls")

(def month-empty         {:month "Fevereiro" :raw-links nil})
(def month-normal        {:month "Janeiro"   :raw-links (seq [raw-link-05 raw-link-30])})
(def month-consolidated  {:month "Março"     :raw-links (seq [raw-link-05 raw-link-total])})

(fact "sptrans/daily-links is a list of all available links on a single month"
  (sptrans/daily-links month-empty)  => '()
  (sptrans/daily-links month-normal) => (seq [{:date (time/local-date date-format "2019-01-05")
                                               :url "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/05012019.xls"}
                                              {:date (time/local-date date-format "2019-01-30")
                                               :url "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/30SET2019.xls"}])
  (sptrans/daily-links month-consolidated) => (seq [{:date (time/local-date date-format "2019-03-05")
                                                     :url "https://www.prefeitura.sp.gov.br/cidade/secretarias/upload/05012019.xls"}]))

(defn day-of-year
  [link]
  (time/as (:date link) :day-of-year))

(def mock-page (tools/mock-from-fixture "sptrans/statistics_page.html"))

(fact "sptrans/year-links parses all links for a given year sorted by date"
  (let [stats-links (sptrans/year-links ..url..)]
    (count stats-links) => 273
    (apply <= (map day-of-year stats-links)) => true)
  (against-background (slurp ..url..) => mock-page))

(facts "about sptrans/link-of-date"
  (fact "link is a map representing the given date, if the date is found"
    (:date (sptrans/link-of-date "2019-03-30")) => (time/local-date date-format "2019-03-30")
    (:date (sptrans/link-of-date "2019-06-12")) => (time/local-date date-format "2019-06-12"))

  (fact "link is nil when requested date can't be found"
    (:date (sptrans/link-of-date "2019-10-25")) => nil
    (:date (sptrans/link-of-date "2019-12-10")) => nil)

  (against-background (slurp sptrans/statistics-url) => mock-page))

(def mock-link {:url "foo.com"})
(def mock-sheet (spreadsheet/load-sheet "./test/fixtures/sptrans/type_a.xls"))
(def mock-row (seq (spreadsheet/nth-row mock-sheet 0 :header-size 3)))

(facts "about sptrans/link->statistics"
  (fact "raises error if it does not find a suitable parser to a given row"
    (sptrans/link->statistics mock-link) => (throws ExceptionInfo "No suitable parser")
    (against-background (spreadsheet/load-sheet (:url mock-link)) => mock-sheet
                        (parser-a/parseable? mock-row) => false
                        (parser-b/parseable? mock-row) => false))

    (fact "parses all rows using proper parser"
      (let [statistics (sptrans/link->statistics mock-link)]
        (every? #(contains? % :route) statistics) => true)
      (against-background
       (spreadsheet/load-sheet (:url mock-link)) => mock-sheet)))
