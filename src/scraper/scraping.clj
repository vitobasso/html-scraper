(ns scraper.scraping
    (:require [scraper.template-parsing :as template-parsing])
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn parse-scraper [template]
  (template-parsing/parse-scraper template))

(defn scrape-field [html config]
  (let [path (:path config)
        extract (:extractor config)]
    {(:name config) (first (flatten (map extract (html/select html path))))}))

(defn scrape-item [item config]
  (map #(scrape-field item %) (:fields config)))

(defn scrape-items [html config]
  (map #(scrape-item % config) (html/select html (:items config))))

(defn scrape-page [page-number config]
  (let [url-template (:search-url config)
        url (string/replace url-template #"\$\{PAGE_NUMBER\}" page-number)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-items parsed-html config)))