(ns scraper.core
  (:require [hickory.core :as h]
            [hickory.select :as s]
            [hickory.render :as r]
            [clojure.string :as string]))

(defn trim-value [maybe-value]
  (if (some? maybe-value) (string/trim maybe-value)))

(defn escape-string-for-replacement [s]
  (string/replace s #"([\\\$])" "\\\\$1"))

(defn replace-var [str-template [key value]]
  (if (nil? value) str-template
    (let [pattern (re-pattern (str "\\$\\{" (name key) "\\}"))
          escaped-value (escape-string-for-replacement value)]
      (string/replace str-template pattern escaped-value))))

(defn replace-vars [str-template var-map]
  (reduce replace-var str-template var-map))

(defn replace-indexes [str-template values]
  (let [into-key (comp keyword str)
        counting-keys (map into-key (rest (range)))
        var-map (zipmap counting-keys values)]
    (replace-vars str-template var-map)))

(defn regex-extract [config value]
  (if config
    (let [[match & groups] (re-find (:find config) value)
          template (:replace config)]
      (if match
        (if template
          (replace-indexes template groups)
          value)))
    value))

(defn select [selector html]
  (try (s/select selector html)
       (catch #?(:clj Exception :cljs :default) _ nil)))

(defn extract-value [config html]
  (->> html
       (select (:select config))
       (map (:extract config))
       (flatten)
       (filter string?)
       (map trim-value)
       (remove empty?)
       (map #(regex-extract (:regex config) %))
       (remove nil?)
       (first)))

(defn scrape-property-row [config row]
  (let [label (extract-value (:label config) row)
        value (extract-value (:value config) row)]
    (if (nil? label) {}
      {(keyword label) value})))

(defn scrape-property-table [config full-item]
  (if (nil? config) {}
    (let [rows (select (:pair-select config) full-item)
          labels-values (map #(scrape-property-row config %) rows)]
      (into {} labels-values))))

(defn scrape-item-by-tables [config item]
  (let [scrape-one-table #(scrape-property-table % item)
        all-tables (map scrape-one-table (:property-tables config))]
    (apply merge all-tables)))

(defn scrape-property [config full-item]
  (if (nil? config) {}
    (let [key (keyword (:name config))
          value (extract-value config full-item)]
      {key value})))

(defn scrape-item-by-properties [config item]
  (let [scrape-one-property #(scrape-property % item)
        all-properties (map scrape-one-property (:properties config))]
    (apply merge all-properties)))

(defn scrape-item [config item]
  (merge (scrape-item-by-properties config item)
         (scrape-item-by-tables config item)))

(defn parse-html [html-str]
  (h/as-hickory (h/parse html-str)))

(defn split-items [config container]
  (let [container-html (r/hickory-to-html container)
        split-pattern (re-pattern (:item-split config))
        item-htmls (string/split container-html split-pattern)]
    (map parse-html item-htmls)))

(defn select-items-by-split [config full-page]
  (->> full-page
       (select (:container-select config))
       (filter map?)
       (map #(:content %))
       (flatten)
       (filter map?)
       (map #(split-items config %))
       (flatten)))

(defn select-items [config full-page]
  (if (:item-select config)
    (select (:item-select config) full-page)
    (select-items-by-split config full-page)))

(defn scrape-items [config full-page]
  (let [items (select-items config full-page)]
    (map #(scrape-item config %) items)))

(defn scrape-list [config html]
  (let [list-config (:list-page config)
        parsed-html (parse-html html)]
    (scrape-items list-config parsed-html)))

(defn scrape-detail [config html]
  (let [detail-config (:detail-page config)
        parsed-html (parse-html html)]
    (scrape-item detail-config parsed-html)))