(ns scraper.main
  (:require [scraper.scraping :as scraping])
  (:require [yaml.core :as yaml])
  (:require [clj-http.client :as client])
  (:require [hickory.core :as h])
  (:require [hickory.select :as s]))

(defn print-items [items]
  (doseq [item items]
    (doseq [attribute item]
      (println attribute))))

(def config
  (scraping/parse-config (yaml/from-file "templates/ebay-co-uk.yml")))

(defn page [search-term page-number]
  (print-items (scraping/scrape-list search-term page-number config)))

(defn detail [item]
  (print-items (scraping/scrape-detail item config)))

; uncomment to try manual scraping from repl
;(def list-config (:list-page config))
;(def url-template (:url list-config))
;(def url (scraping/build-search-url url-template "london" 1))
;(def html (-> url client/get :body h/parse h/as-hickory))
;(def items (s/select (:item-selector list-config) html))
;(def item (first items))
;(print-items (scraping/scrape-item item list-config))
;(print-items (scraping/scrape-items html config))