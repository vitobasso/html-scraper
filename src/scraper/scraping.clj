(ns scraper.scraping
    (:require [scraper.template-parsing :as template-parsing])
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn parse-scraper [template]
  (template-parsing/parse-config template))

(defn scrape-field [html config]
  (let [path (:path config)
        extract (:extractor config)]
    {(:name config) (first (flatten (map extract (html/select html path))))}))

(defn scrape-item [item config]
  (map #(scrape-field item %) (:fields config)))

(defn scrape-items [html config]
  (map #(scrape-item % config) (html/select html (:items config))))

(defn interpolate-url [url-template search-term page-number]
  (let [items-per-page 25 ;; TODO get from config
        page-offset (* (- page-number 1) items-per-page)]
    (-> url-template
        (string/replace #"\$\{SEARCH_TERM\}" search-term)
        (string/replace #"\$\{PAGE_NUMBER\}" (str page-number))
        (string/replace #"\$\{ITEMS_PER_PAGE\}" (str items-per-page))
        (string/replace #"\$\{PAGE_OFFSET\}" (str page-offset)))))

(defn scrape-page [search-term page-number config]
  (let [url-template (:search-url config)
        url (interpolate-url url-template search-term page-number)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-items parsed-html config)))