(ns scraper.config
    (:require [hickory-css-selectors :as css])
    (:require [clojure.string :as string]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn- parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn- parse-attribute [src]
  {:name (:name src)
   :selector (css/parse-css-selector (:selector src))
   :extractor (parse-extractor (:extractor src))
   :regex (:regex src)})

(defn- parse-attributes [src]
  (map parse-attribute (:attributes src)))

(defn parse-config [src] ;; TODO validate paging params
  (let [list-page (:list-page src)
        detail-page (:detail-page src)
        get-url #(str (:home-url src) (:url-path %))]
    {:home-url (:home-url src)
     :list-page {
       :url (get-url list-page)
       :item-selector (css/parse-css-selector (:item-selector list-page))
       :attributes (parse-attributes list-page)}
     :detail-page {
       :url (get-url detail-page)
       :attributes (parse-attributes detail-page)}}))
