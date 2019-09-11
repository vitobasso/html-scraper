(ns scraper.endpoints
  (:require [scraper.scraping :as scr])
  (:require [scraper.config :as cfg]))

(defn sites []
  (cfg/list-configs))

(defn page [site search-term page-number]
  (let [config (cfg/load-config site)]
        (scr/scrape-list search-term page-number config)))

(defn detail [site basic-item]
  (let [config (cfg/load-config site)]
        (scr/scrape-detail basic-item config)))
