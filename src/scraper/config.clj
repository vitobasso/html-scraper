(ns scraper.config
    (:require [hickory-css-selectors :as css])
    (:require [clojure.string :as string]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn- parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn parse-attribute [src]
  {:name (:name src)
   :selector (css/parse-css-selector (:selector src))
   :extractor (parse-extractor (:extractor src))
   :regex (:regex src)})

(defn- parse-attributes [src]
  (map parse-attribute (:attributes src)))

(defn parse-list-page [src home-url]
  {:url (str home-url (:url-path src)) ;; TODO validate paging params
   :item-selector (css/parse-css-selector (:item-selector src))
   :attributes (parse-attributes src)})

(defn parse-detail-page [src home-url]
  {:url (str home-url (:url-path src))
   :attributes (parse-attributes src)})

(defn parse-config [src]
  (let [home-url (:home-url src)]
    {:home-url    home-url
     :list-page   (parse-list-page (:list-page src) home-url)
     :detail-page (parse-detail-page (:detail-page src) home-url)}))
