(ns scraper.config
  (:require [hickory-css-selectors :as css])
  (:require [clojure.string :as string])
  (:require [yaml.core :as yaml]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn- parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn parse-attribute [src]
  {:selector (css/parse-css-selector (:selector src))
   :extractor (parse-extractor (:extractor src))
   :regex (:regex src)})

(defn parse-named-attribute [src]
  (merge {:name (:name src)}
         (parse-attribute src)))

(defn parse-attribute-table [src]
  (if (nil? src) nil
    (let [label (:label src)
          value (:value src)]
      {:selector (css/parse-css-selector (:selector src))
       :label    (parse-attribute label)
       :value    (parse-attribute value)})))

(defn parse-detail-page [src home-url]
  {:url (str home-url (:url-path src)) ; TODO validate paging params
   :attributes (map parse-named-attribute (:attributes src))
   :attribute-table (parse-attribute-table (:attribute-table src))})

(defn parse-list-page [src home-url]
  (merge (parse-detail-page src home-url)
          {:item-selector (css/parse-css-selector (:item-selector src))}))

(defn parse-config [src]
  (let [home-url (:home-url src)]
    {:home-url    home-url
     :list-page   (parse-list-page (:list-page src) home-url)
     :detail-page (parse-detail-page (:detail-page src) home-url)}))

(defn load-config [name]
  (-> (str "templates/" name ".yml")
      yaml/from-file
      parse-config))

(defn extract-site-name [file-name]
  (let [[_ site-name] (re-find #"(.*)\.ya?ml" file-name)]
    site-name))

(defn list-configs []
  (->> (clojure.java.io/file "templates")
       .listFiles
       (map #(.getName %))
       (map extract-site-name)
       (remove nil?)))