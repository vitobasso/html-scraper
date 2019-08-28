(ns scraper.template-parsing
    (:require [net.cgrand.enlive-html :as html]))

(defn split-by-space [str]
  (clojure.string/split str #" "))

(defn parse-selector-word [str] ;; TODO handle tag.class (intersection) and other tag:function
  (let [[matches tag num] (re-find #"(.*):nth-child\((\d+)\)" str)]
    (if matches
      [(keyword tag) (html/nth-child (Integer/parseInt num))]
      (keyword str))))

(defn parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn parse-selector [str]
  (into [] (map parse-selector-word (split-by-space str))))

(defn parse-field-config [src]
  {:name (:name src)
   :path (parse-selector (:path src))
   :extractor (parse-extractor (:extractor src))})

(defn parse-config [src]
  {:search-url (:search-url src)
   :items (parse-selector (:items src))
   :fields (map parse-field-config (:fields src))})
