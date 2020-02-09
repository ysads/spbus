(ns spbus.adapters.sptrans.parser-a
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [spbus.adapters.spreadsheet :as spreadsheet])
  (:gen-class))

(def terminus-first-char-index 9)
(def terminus-inter-separator #"(-)|(/)")
(def preboarding-tokens ["TSATER" "EXP TIRADENTES" "5105"])

(defn ^:private line-code
  "Returns the line main code. Examples:
   - 803210 => 8032
   - N81211 => N812"
  [id]
  (.substring (java.lang.String. id) 0 (- (count id) 2)))

(defn ^:private branch-code
  "Returns the line branch code. Examples:
   - 803210 => 10
   - N81211 => 11"
  [id]
  (.substring (java.lang.String. id)
              (- (count id) 2)
              (count id)))

(defn ^:private route
  "The full line route, potentially including main and auxiliar
  terminuses as long as some pre-boarding keywords."
  [row]
  (let [content (spreadsheet/cell-value row 4)]
    (str/upper-case (subs content terminus-first-char-index))))

(defn ^:private route-include-pre-boarding-token?
  "Returns true if line route contains any of the tokens
  which indicates a pre-boarding."
  [row]
  (let [route (route row)]
    (reduce (fn [result token]
              (or result (str/includes? route token)))
            false
            preboarding-tokens)))

(defn line-id
  "The line unique and distinct ID."
  [row]
  (let [content (spreadsheet/cell-value row 4)]
    (first (str/split content #" - "))))

(defn pretty-line-id
  "The line ID formatted as usually seen on buses and media,
  taking the stance that branch codes are separated from main code."
  [row]
  (let [raw-id (line-id row)]
    (str (line-code raw-id) "-" (branch-code raw-id))))

(defn main-terminus
  [row]
  (let [line-route (route row)
        split-terminus (str/split line-route terminus-inter-separator)]
    (str/trim (first split-terminus))))

(defn auxiliar-terminus
  [row]
  (let [line-route (route row)
        split-terminus (str/split line-route terminus-inter-separator)]
    (str/trim (last split-terminus))))

(defn company
  [row]
  (spreadsheet/cell-value row 3))

(defn area
  [row]
  (str (last (spreadsheet/cell-value row 2))))

(defn paying-cash-pax
  [row]
  (int (spreadsheet/cell-value row 5)))

(defn paying-normal-work-card-pax
  [row]
  (int (spreadsheet/cell-value row 6)))

(defn paying-student-pax
  [row]
  (int (spreadsheet/cell-value row 8)))

(defn paying-month-pass-normal-pax
  [row]
  (int (spreadsheet/cell-value row 7)))

(defn paying-month-pass-student-pax
  [row]
  (int (spreadsheet/cell-value row 9)))

(defn paying-month-pass-work-pax
  [row]
  (int (spreadsheet/cell-value row 10)))

(defn paying-pax
  [row]
  (int (spreadsheet/cell-value row 11)))

(defn free-bus-bus-conn-pax
  [row]
  (int (spreadsheet/cell-value row 12)))

(defn free-normal-pax
  [row]
  (int (spreadsheet/cell-value row 13)))

(defn free-student-pax
  [row]
  (int (spreadsheet/cell-value row 14)))

(defn total-pax
  [row]
  (int (spreadsheet/cell-value row 15)))

(defn free-pax
  [row]
  (- (total-pax row) (paying-pax row)))

(defn parseable?
  [row]
  (= 16 (count row)))

(defn pre-boarding?
  [row]
  (let [id (line-id row)]
    (or (some? (re-matches #"^([0-9]+PR).*" id))
        (route-include-pre-boarding-token? row))))

(defn basic-info
  [row]
  {:company (company row)
    :area (area row)
    :transport-mode "bus"})

(defn with-pre-boarding
  [data row]
  (merge data {:pre-boarding (pre-boarding? row)}))

(defn with-stop-details
  [data row]
  (if (pre-boarding? row)
    (merge data {:stop-name (route row)
                 :stop-id (line-id row)})
    data))

(defn with-route-details
  [data row]
  (let [id (line-id row)]
    (if (pre-boarding? row)
      data
      (merge data {:route (route row)
                   :main-terminus (main-terminus row)
                   :auxiliar-terminus (auxiliar-terminus row)
                   :line-id id
                   :line-code (line-code id)
                   :branch-code (branch-code id)}))))

(defn with-pax-totals
  [data row]
  (merge data {:paying-pax {:cash (paying-cash-pax row)
                            :normal-and-work-card (paying-normal-work-card-pax row)
                            :student (paying-student-pax row)
                            :month-pass-normal (paying-month-pass-normal-pax row)
                            :month-pass-work (paying-month-pass-work-pax row)
                            :month-pass-student (paying-month-pass-student-pax row)
                            :total (paying-pax row)}
               :free-pax {:bus-bus-connections (free-bus-bus-conn-pax row)
                          :normal (free-normal-pax row)
                          :student (free-student-pax row)
                          :total (free-pax row)}
               :total-pax (total-pax row)}))

(defn parse
  "Returns a map containing all the line information"
  [row]
  (-> (basic-info row)
      (with-pre-boarding row)
      (with-stop-details row)
      (with-route-details row)
      (with-pax-totals row)))
