(ns spbus.adapters.sptrans
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [java-time :as time]
            [reaver :as r]
            [spbus.adapters.sptrans.parser-a :as parser-a]
            [spbus.adapters.sptrans.parser-b :as parser-b]
            [spbus.adapters.spreadsheet :as spreadsheet])
  (:gen-class))

(def statistics-url "https://www.prefeitura.sp.gov.br/cidade/secretarias/transportes/institucional/sptrans/acesso_a_informacao/agenda/index.php?p=269652")

(def months-mapping {"Janeiro" "01"
                     "Fevereiro" "02"
                     "Março" "03"
                     "Abril" "04"
                     "Maio" "05"
                     "Junho" "06"
                     "Julho" "07"
                     "Agosto" "08"
                     "Setembro" "09"
                     "Outubro" "10"
                     "Novembro" "11"
                     "Dezembro" "12"})

(defn month
  "Simply convert a long and descritive month to its numeric representation."
  [month-str]
  (get months-mapping month-str))

(defn ^:private stats-on-container
  [page container]
  (r/extract-from (r/parse page)
                  container
                  [:month :raw-links]
                  "caption" r/text
                  "a"       r/edn))

(defn month-total?
  "Is true when the given raw link represents statistics consolidated
  for a whole month, instead of a single day."
  [raw-link]
  (let [day-str (first (:content raw-link))
        sanitized-day (str/replace day-str #"\u00a0" "")]
    (str/includes? "Total" sanitized-day)))

(defn ^:private formatted-date
  [day-str month-str]
  (str "2019-" (month month-str) "-" day-str))

(defn link-date
  "Returns the date to which a link refer in a proper format."
  [raw-link month-str]
  (if-not (month-total? raw-link)
    (let [day-str (first (:content raw-link))
          date-str (formatted-date day-str month-str)]
      (time/local-date "yyyy-MM-dd" date-str))
    nil))

(defn link-url
  "Returns the URL in which the stats are available."
  [raw-link]
  (:href (:attrs raw-link)))

(defn ^:private new-link
  "Represents a link to a particular day's stats. Note we won't
  allow consolidated results to be parsed, since they are not tied
  to any particular day."
  [raw-link month]
  (when-not (month-total? raw-link)
    {:date (link-date raw-link month)
     :url  (link-url raw-link)}))

(defn daily-links
  "All available links for statistics of a particular month."
  [raw-month]
  (let [month (:month raw-month)
        links (or (:raw-links raw-month) [])]
    (->> links
         (map #(new-link % month))
         (remove nil?))))

(defn year-links
  "All available links for statistics of a year."
  [url]
  (let [page (slurp url)]
    (->> (into (stats-on-container page ".calend_dir")
               (stats-on-container page ".calend_esq"))
         (map daily-links)
         (flatten)
         (sort-by #(time/as (:date %) :day-of-year)))))

(defn link-of-date
  "Returns the link for a given date."
  [date]
  (->> (year-links statistics-url)
       (filter #(= (:date %) (time/local-date date)))
       (first)))

(defn ^:private row->route-statistics
  [raw-row]
  (let [row (seq raw-row)]
    (cond
      (parser-a/parseable? row) (parser-a/parse row)
      (parser-b/parseable? row) (parser-b/parse row)
      :else (throw (ex-info "No suitable parser" {:row row})))))

(defn ^:private all-statistics
  [rows]
  (map row->route-statistics rows))

(defn link->statistics
  "Parses all available statistics based on a given link. These statistics
  are parsed according to their proper parsers."
  [link]
  (-> (spreadsheet/load-sheet (:url link))
      (spreadsheet/rows :header-size 3)
      (all-statistics)))
