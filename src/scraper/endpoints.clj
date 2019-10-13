(ns scraper.endpoints
  (:require [scraper.scraping :as scr])
  (:require [scraper.config :as cfg]))

(defn sites []
  (cfg/list-configs))

(defn page [site-name params]
  (let [config (cfg/load-config site-name)]
        (scr/scrape-list config params)))

(defn detail [site basic-item]
  (let [config (cfg/load-config site)]
        (scr/scrape-detail config basic-item)))
