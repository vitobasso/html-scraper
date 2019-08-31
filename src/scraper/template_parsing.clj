(ns scraper.template-parsing
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn parse-selector-word [str] ;; TODO handle tag.class (intersection) and other tag:function
  (let [[matches tag num] (re-find #"(.*):nth-child\((\d+)\)" str)]
    (if matches
      [(keyword tag) (html/nth-child (Integer/parseInt num))]
      (keyword str))))

(defn- parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn- parse-selector [str]
  (into [] (map parse-selector-word (split-by-space str))))

(defn- parse-attribute [src]
  {:name (:name src)
   :selector (parse-selector (:selector src))
   :extractor (parse-extractor (:extractor src))})

(defn- parse-attributes [src]
  (map parse-attribute (:attributes src)))

(defn parse-config [src] ;; TODO validate paging params
  (let [list-page (:list-page src)
        detail-page (:detail-page src)]
    {:home-url (:home-url src)
     :list-page {
       :search-url (str (:home-url src) (:url-path list-page))
       :item-selector (parse-selector (:item-selector list-page))
       :attributes (parse-attributes list-page)}
     :detail-page {
       :attributes (parse-attributes detail-page)}}))
