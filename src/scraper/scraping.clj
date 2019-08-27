(ns scraper.scraping
    (:require [scraper.template-parsing :as template-parsing])
    (:require [net.cgrand.enlive-html :as html]))

(defn parse-scraper [template]
  (template-parsing/parse-scraper template))

(defn scrape-field [html scraper]
  (let [path (:path scraper)
        extract (:extractor scraper)]
    {(:name scraper) (first (flatten (map extract (html/select html path))))}))

(defn scrape-item [item scraper]
  (map #(scrape-field item %) (:fields scraper)))

(defn scrape-items [html scraper]
  (map #(scrape-item % scraper) (html/select html (:items scraper))))
