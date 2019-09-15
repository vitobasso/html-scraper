(ns scraper.scraping
    (:require [hickory.core :as h])
    (:require [hickory.select :as s])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn trim-value [maybe-value]
  (if (some? maybe-value) (string/trim maybe-value)))

(defn replace-var [str-template [key value]]
  (let [pattern (re-pattern (str "\\$\\{" (name key) "\\}"))
        literal-value (string/re-quote-replacement value)]
    (string/replace str-template pattern literal-value)))

(defn replace-vars [str-template var-map]
  (reduce replace-var str-template var-map))

(defn replace-indexes [str-template values]
  (let [into-key (comp keyword str)
        counting-keys (map into-key (rest (range)))
        var-map (zipmap counting-keys values)]
    (replace-vars str-template var-map)))

(defn regex-extract [value config]
  (if config
    (let [pattern (re-pattern (:find config))
          [match & groups] (re-find pattern value)
          template (:replace config)]
      (if match
        (if template
          (replace-indexes template groups)
          value)))
    value))

(defn extract-value [config html]
  (->> html
       (s/select (:selector config))
       (map (:extractor config))
       (flatten)
       (filter string?)
       (map trim-value)
       (remove empty?)
       (map #(regex-extract % (:regex config)))
       (remove nil?)
       (first)))

(defn scrape-attribute-row [row config]
  (let [label (extract-value (:label config) row)
        value (extract-value (:value config) row)]
    {(keyword label) value}))

(defn scrape-attribute-table [full-item config]
  (if (nil? config) {}
    (let [rows (s/select (:selector config) full-item)
          labels-values (map #(scrape-attribute-row % config) rows)]
      (into {} labels-values))))

(defn scrape-item-by-table [item config]
  (scrape-attribute-table item (:attribute-table config)))

(defn scrape-attribute [full-item config]
  (if (nil? config) {}
    (let [key (keyword (:name config))
          value (extract-value config full-item)]
      {key value})))

(defn scrape-item-by-attrs [item config]
  (let [attr-config (:attributes config)
        scrape-one-attr #(scrape-attribute item %)
        all-attrs (map scrape-one-attr attr-config)]
    (apply merge all-attrs)))

(defn scrape-item [item config]
  (merge (scrape-item-by-attrs item config)
         (scrape-item-by-table item config)))

(defn scrape-items [full-page config]
  (let [items (s/select (:item-selector config) full-page)]
    (map #(scrape-item % config) items)))

(defn build-search-url [url-template search-term page-number]
  (let [items-per-page 25 ;; TODO get from config
        page-offset (* (- page-number 1) items-per-page)
        vars {:search-term search-term
              :page-number (str page-number)
              :items-per-page (str items-per-page)
              :page-offset (str page-offset)}]
    (replace-vars url-template vars)))

(defn parse-html [html-str]
  (h/as-hickory (h/parse html-str)))

(defn scrape-list [search-term page-number config]
  (let [list-config (:list-page config)
        url-template (:url list-config)
        url (build-search-url url-template search-term page-number)
        response-body (:body (client/get url))
        parsed-html (parse-html response-body)]
    (prn "scrape-list: " url)
    (scrape-items parsed-html list-config)))

(defn scrape-detail [item-kv config]
  (let [detail-config (:detail-page config)
        url-template (:url detail-config)
        url (replace-vars url-template item-kv)
        response-body (:body (client/get url))
        parsed-html (parse-html response-body)]
    (prn "scrape-detail: " url)
    (scrape-item parsed-html detail-config)))