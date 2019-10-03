(ns scraper.scraping
    (:require [hickory.core :as h])
    (:require [hickory.select :as s])
    (:require [hickory.render :as r])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn trim-value [maybe-value]
  (if (some? maybe-value) (string/trim maybe-value)))

(defn replace-var [str-template [key value]]
  (if (nil? value) str-template
    (let [pattern (re-pattern (str "\\$\\{" (name key) "\\}"))
          literal-value (string/re-quote-replacement value)]
      (string/replace str-template pattern literal-value))))

(defn replace-vars [str-template var-map]
  (reduce replace-var str-template var-map))

(defn replace-indexes [str-template values]
  (let [into-key (comp keyword str)
        counting-keys (map into-key (rest (range)))
        var-map (zipmap counting-keys values)]
    (replace-vars str-template var-map)))

(defn regex-extract [value config]
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
       (catch Exception _ nil)))

(defn extract-value [config html]
  (->> html
       (select (:select config))
       (map (:extract config))
       (flatten)
       (filter string?)
       (map trim-value)
       (remove empty?)
       (map #(regex-extract % (:regex config)))
       (remove nil?)
       (first)))

(defn scrape-property-row [row config]
  (let [label (extract-value (:label config) row)
        value (extract-value (:value config) row)]
    (if (nil? label) {}
      {(keyword label) value})))

(defn scrape-property-table [full-item config]
  (if (nil? config) {}
    (let [rows (select (:pair-select config) full-item)
          labels-values (map #(scrape-property-row % config) rows)]
      (into {} labels-values))))

(defn scrape-item-by-table [item config]
  (scrape-property-table item (:property-table config)))

(defn scrape-property [full-item config]
  (if (nil? config) {}
    (let [key (keyword (:name config))
          value (extract-value config full-item)]
      {key value})))

(defn scrape-item-by-properties [item config]
  (let [scrape-one-property #(scrape-property item %)
        all-properties (map scrape-one-property (:properties config))]
    (apply merge all-properties)))

(defn scrape-item [item config]
  (merge (scrape-item-by-properties item config)
         (scrape-item-by-table item config)))

(defn parse-html [html-str]
  (h/as-hickory (h/parse html-str)))

(defn split-items [container config]
  (let [container-html (r/hickory-to-html container)
        split-pattern (re-pattern (:item-split config))
        item-htmls (string/split container-html split-pattern)]
    (map parse-html item-htmls)))

(defn select-items-by-split [full-page config]
  (->> full-page
       (select (:container-select config))
       (filter map?)
       (map #(:content %))
       (flatten)
       (filter map?)
       (map #(split-items % config))
       (flatten)))

(defn select-items [full-page config]
  (if (:item-select config)
    (select (:item-select config) full-page)
    (select-items-by-split full-page config)))

(defn scrape-items [full-page config]
  (let [items (select-items full-page config)]
    (map #(scrape-item % config) items)))

(defn build-search-url [url-template search-term page-number]
  (let [items-per-page 25 ;; TODO get from config
        page-offset (* (- page-number 1) items-per-page)
        vars {:search-term search-term
              :page-number (str page-number)
              :items-per-page (str items-per-page)
              :page-offset (str page-offset)}]
    (replace-vars url-template vars)))

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