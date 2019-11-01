(ns scraper.config
  (:require [hickory.css-selectors :as css])
  (:require [clojure.string :as string]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn- parse-extractor [str]
  (if (nil? str) :content
    (apply comp (reverse (map keyword (split-by-space str))))))

(defn parse-regex [src]
  (if src
    {:find (-> src :find re-pattern)
     :replace (-> src :replace)}))

(defn parse-property [src]
  {:select (css/parse-css-selector (:select src))
   :extract (parse-extractor (:extract src))
   :regex (parse-regex (:regex src))})

(defn parse-named-property [src]
  (merge {:name (:name src)}
         (parse-property src)))

(defn parse-property-table [src]
  (if (nil? src) nil
    (let [label (:label src)
          value (:value src)]
      {:pair-select (css/parse-css-selector (:pair-select src))
       :label  (parse-property label)
       :value  (parse-property value)})))

(defn parse-detail-page [src]
  {:url (:url src) ; TODO validate paging params
   :properties (map parse-named-property (:properties src))
   :property-tables (map parse-property-table (:property-tables src))})

(defn parse-item-select [src]
  (let [selector (:item-select src)]
    (if (some? selector)
      {:item-select (css/parse-css-selector selector)})))

(defn parse-item-split [src]
  (let [container (:container-select src)
        split-pattern (:item-split src)]
    (cond (and container split-pattern)
            {:container-select (css/parse-css-selector container)
             :item-split (re-pattern split-pattern)}
          container (throw (Exception. "container-select is defined but item-split is missing."))
          split-pattern (throw (Exception. "item-split was defined defined but container-select is missing.")))))

(defn parse-item-selection [src]
  (let [selector (parse-item-select src)
        split-pattern (parse-item-split src)]
    (cond
      (and selector split-pattern) (throw (Exception. "Can't have both item-select and item-split defined."))
      (not (or selector split-pattern)) (throw (Exception. "Either item-select or item-split must be defined."))
      :else (or selector split-pattern))))

(defn parse-list-page [src]
  (merge (parse-detail-page src)
         (parse-item-selection src)))

(defn parse-config [src]
  {:list-page   (parse-list-page (:list-page src))
   :detail-page (parse-detail-page (:detail-page src))})
