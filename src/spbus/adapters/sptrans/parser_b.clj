(ns spbus.adapters.sptrans.parser-b
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [spbus.adapters.spreadsheet :as spreadsheet])
  (:gen-class))

(def terminus-first-char-index 9)
(def terminus-inter-separator #"(-)|(/)")
(def terminus-preboarding-tokens ["TSATER" "EXP TIRADENTES" "5105"])

(defn ^:private line-code
  "Returns the line main code. Examples:
   - 803210 => 8032
   - N81211 => N812"
  [id]
  (.substring (java.lang.String. id) 0 (- (count id) 2)))

(defn ^:private branch-code
  "Returns the line main code. Examples:
   - 803210 => 10
   - N81211 => 11"
  [id]
  (.substring (java.lang.String. id)
              (- (count id) 2)
              (count id)))

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

(defn ^:private line-terminus
  "The full line terminus, potentially including main and auxiliar
  terminuses as long as some pre-boarding keywords."
  [row]
  (let [content (spreadsheet/cell-value row 4)]
    (str/upper-case (subs content terminus-first-char-index))))

(defn ^:private terminus-include-pre-boarding-token?
  "Returns true if line terminus contains any of the tokens
  which indicates a pre-boarding."
  [row]
  (let [terminus (line-terminus row)]
    (reduce (fn [result token]
              (or result (str/includes? terminus token)))
            false
            terminus-preboarding-tokens)))


(defn main-terminus
  [row]
  (let [terminus (line-terminus row)]
    (str/trim (first (str/split terminus terminus-inter-separator)))))

(defn auxiliar-terminus
  [row]
  (let [terminus (line-terminus row)]
				(str/trim (last (str/split terminus terminus-inter-separator)))))

(defn company
  [row]
  (spreadsheet/cell-value row 3))

(defn paying-cash-pax
  [row]
  (int (spreadsheet/cell-value row 5)))

(defn paying-normal-pax
  [row]
  (int (spreadsheet/cell-value row 6)))

(defn paying-work-card-pax
  [row]
  (int (spreadsheet/cell-value row 10)))

(defn paying-student-pax
  [row]
  (int (spreadsheet/cell-value row 8)))

(defn paying-rail-bus-conn-pax
  [row]
  (int (spreadsheet/cell-value row 12)))

(defn paying-month-pass-normal-pax
  [row]
  (int (spreadsheet/cell-value row 7)))

(defn paying-month-pass-work-pax
  [row]
  (int (spreadsheet/cell-value row 11)))

(defn paying-month-pass-student-pax
  [row]
  (int (spreadsheet/cell-value row 9)))

(defn paying-month-pass-rail-bus-conn-pax
  [row]
  (int (spreadsheet/cell-value row 13)))

(defn paying-pax
  [row]
  (int (spreadsheet/cell-value row 14)))

(defn free-bus-bus-conn-pax
  [row]
  (int (spreadsheet/cell-value row 15)))

(defn free-normal-pax
  [row]
  (int (spreadsheet/cell-value row 16)))

(defn free-student-pax
  [row]
  (int (spreadsheet/cell-value row 17)))

(defn total-pax
  [row]
  (int (spreadsheet/cell-value row 18)))

(defn free-pax
  [row]
  (- (total-pax row) (paying-pax row)))

(defn parseable?
  [row]
  (= 19 (count row)))

(defn pre-boarding?
  [row]
  (let [id (line-id row)]
    (or (some? (re-matches #"^([0-9]+PR).*" id))
        (terminus-include-pre-boarding-token? row))))

(defn basic-info
  [row]
  (let [id (line-id row)]
    {:company (company row)
     :line-id id
     :line-code (line-code id)
     :branch-code (branch-code id)}))

(defn with-pre-boarding
  [data row]
  (merge data {:pre-boarding (pre-boarding? row)}))

(defn with-terminus
  [data row]
  (merge data (if (pre-boarding? row)
                  {:terminus (main-terminus row)}
                  {:main-terminus (main-terminus row)
                   :auxiliar-terminus (auxiliar-terminus row)})))

(defn with-pax-totals
  [data row]
  (merge data {:paying-pax {:cash (paying-cash-pax row)
										                  :normal (paying-normal-pax row)
										                  :work-card (paying-work-card-pax row)
										                  :student (paying-student-pax row)
										                  :rail-bus-connections (paying-rail-bus-conn-pax row)
										                  :month-pass-normal (paying-month-pass-normal-pax row)
										                  :month-pass-work (paying-month-pass-work-pax row)
										                  :month-pass-student (paying-month-pass-student-pax row)
										                  :month-pass-rail-bus-connections (paying-month-pass-rail-bus-conn-pax row)
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
      (with-terminus row)
      (with-pax-totals row)))
