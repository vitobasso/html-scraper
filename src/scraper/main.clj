(ns scraper.main
  (:require [scraper.scraping :as scr])
  (:require [scraper.config :as cfg])
  (:require [clj-http.client :as cli])
  (:require [hickory.core :as h])
  (:require [hickory.select :as s]))

(defn print-items [items]
  (doseq [item items] (println item)))

(defn sites []
  (cfg/list-configs))

(defn page [site search-term page-number]
  (let [config (cfg/load-config site)
        items (scr/scrape-list search-term page-number config)]
    (print-items items)))

(defn detail [site basic-item]
  (let [config (cfg/load-config site)
        detailed-item (scr/scrape-detail basic-item config)]
    (println detailed-item)))

; uncomment to try manual scraping from repl
;(def list-config (:list-page config))
;(def url-template (:url list-config))
;(def url (scr/build-search-url url-template "london" 1))
;(def html (-> url cli/get :body h/parse h/as-hickory))
;(def items (s/select (:item-selector list-config) html))
;(def item (first items))
;(print-items (scr/scrape-item item list-config))
;(print-items (scr/scrape-items html config))