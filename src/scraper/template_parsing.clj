(ns scraper.template-parsing
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string]))

(defn split-by-space [str]
  (string/split str #" "))

(defn parse-selector-word [str] ;; TODO handle tag.class (intersection) and other tag:function
  (let [[matches tag num] (re-find #"(.*):nth-child\((\d+)\)" str)]
    (if matches
      [(keyword tag) (html/nth-child (Integer/parseInt num))]
      (keyword str))))

(defn parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn parse-selector [str]
  (into [] (map parse-selector-word (split-by-space str))))

(defn parse-attribute-config [src]
  {:name (:name src)
   :selector (parse-selector (:selector src))
   :extractor (parse-extractor (:extractor src))})

(defn parse-config [src] ;; TODO validate paging params
  (let [list-page (:list-page src)]
    {:search-url (str (:host src) (:url list-page))
     :item-selector (parse-selector (:item-selector list-page))
     :attributes (map parse-attribute-config (:attributes list-page))}))
