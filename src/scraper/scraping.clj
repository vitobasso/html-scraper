(ns scraper.scraping
    (:require [scraper.template-parsing :as template-parsing])
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn parse-scraper [template]
  (template-parsing/parse-config template))

(defn trim-value [maybe-value]
  (if (some? maybe-value) (string/trim maybe-value) maybe-value))

(defn scrape-attribute [full-html config]
  (let [selector (:selector config)
        extract (:extractor config)
        item-html (html/select full-html selector)
        value (-> (map extract item-html)
                  (flatten)
                  (first)
                  (trim-value))]
    {(:name config) value}))

(defn scrape-item [item config]
  (map #(scrape-attribute item %) (:attributes config)))

(defn scrape-items [html config]
  (map #(scrape-item % config) (html/select html (:item-selector config))))

(defn interpolate-url [url-template search-term page-number]
  (let [items-per-page 25 ;; TODO get from config
        page-offset (* (- page-number 1) items-per-page)]
    (-> url-template
        (string/replace #"\$\{SEARCH_TERM\}" search-term)
        (string/replace #"\$\{PAGE_NUMBER\}" (str page-number))
        (string/replace #"\$\{ITEMS_PER_PAGE\}" (str items-per-page))
        (string/replace #"\$\{PAGE_OFFSET\}" (str page-offset)))))

(defn scrape-list [search-term page-number config]
  (let [list-config (:list-page config)
        url-template (:search-url list-config)
        url (interpolate-url url-template search-term page-number)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-items parsed-html list-config)))

(defn scrape-detail [url-path config]
  (let [url (str (:home-url config) url-path)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-item parsed-html (:detail-page config))))