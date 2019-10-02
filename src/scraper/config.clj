(ns scraper.config
  (:require [hickory-css-selectors :as css])
  (:require [clojure.string :as string])
  (:require [yaml.core :as yaml]))

(defn- split-by-space [str]
  (string/split str #" "))

(defn- parse-extractor [str]
  (if (nil? str) :content
    (apply comp (reverse (map keyword (split-by-space str))))))

(defn parse-regex [src]
  (if src
    {:find (-> src :find re-pattern)
     :replace (-> src :replace)}))

(defn parse-attribute [src]
  {:selector (css/parse-css-selector (:selector src))
   :extractor (parse-extractor (:extractor src))
   :regex (parse-regex (:regex src))})

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

(defn parse-item-selector [src]
  (let [selector (:item-selector src)]
    (if (some? selector)
      {:item-selector (css/parse-css-selector selector)})))

(defn parse-item-separator [src]
  (let [container (:container-selector src)
        separator (:item-separator src)]
    (cond (and container separator)
            {:container-selector (css/parse-css-selector container)
             :item-separator (re-pattern separator)}
          container (throw (Exception. "container-selector is defined but item-separator is missing."))
          separator (throw (Exception. "item-separator was defined defined but container-selector is missing.")))))

(defn parse-item-selection [src]
  (let [selector (parse-item-selector src)
        separator (parse-item-separator src)]
    (cond
      (and selector separator) (throw (Exception. "Can't have both item-selector and item-separator defined."))
      (not (or selector separator)) (throw (Exception. "Either item-selector or item-separator must be defined."))
      :else (or selector separator))))

(defn parse-list-page [src home-url]
  (merge (parse-detail-page src home-url)
         (parse-item-selection src)))

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