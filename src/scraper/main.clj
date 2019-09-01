(ns scraper.main
    (:require [scraper.scraping :as scraping])
    (:require [yaml.core :as yaml])
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html]))

(defn print-items [items]
  (doseq [item items]
    (doseq [attribute item]
      (println attribute))))

(def config
  (scraping/parse-scraper (yaml/from-file "templates/booking.yml")))

(defn page [search-term page-number]
  (print-items (scraping/scrape-list search-term page-number config)))

(defn detail [item]
  (print-items (scraping/scrape-detail item config)))

; uncomment to try manual scraping from repl
;(def list-config (:list-page config))
;(def url-template (:url list-config))
;(def url (scraping/build-search-url url-template "london" 1))
;(def html (html/html-snippet (:body (client/get url))))
;(def items (html/select html  (:item-selector list-config)))
;(def item (first items))
;(print-items (scraping/scrape-item item list-config))
;(def scrape-and-print (print-items (scraping/scrape-items html config)))
