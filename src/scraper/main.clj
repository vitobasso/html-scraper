(ns scraper.main
    (:require [scraper.scraping :as scraping])
    (:require [yaml.core :as yaml])
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html]))

;(def template (yaml/from-file "templates/amazon-co-uk.yml"))
;
;(def ^:dynamic *base-url*
;  "https://www.amazon.co.uk/s?k=cardboard+boxes&crid=2GG8KOUJMLJRY&sprefix=cardboard%2Caps%2C143&ref=nb_sb_ss_i_1_9")

(def template (yaml/from-file "templates/booking.yml"))

(def ^:dynamic *base-url*
  "https://www.booking.com/searchresults.en-us.html?label=gen173nr-1FCAEoggI46AdIM1gEaFCIAQGYATG4AQfIAQzYAQHoAQH4AQKIAgGoAgO4AobomusFwAIB&sid=e0eddec745517c5943b68c9173ce7e52&sb=1&src=index&src_elem=sb&error_url=https%3A%2F%2Fwww.booking.com%2Findex.html%3Flabel%3Dgen173nr-1FCAEoggI46AdIM1gEaFCIAQGYATG4AQfIAQzYAQHoAQH4AQKIAgGoAgO4AobomusFwAIB%3Bsid%3De0eddec745517c5943b68c9173ce7e52%3Bsb_price_type%3Dtotal%26%3B&ss=london&is_ski_area=0&checkin_year=&checkin_month=&checkout_year=&checkout_month=&group_adults=2&group_children=0&no_rooms=1&b_h4u_keep_filters=&from_sf=1")

(defn fetch-url [url]
  (html/html-snippet (:body (client/get url))))

(defn print-items [items]
  (doseq [item items]
    (doseq [field item]
      (println field))))

(def scraper
  (scraping/parse-scraper template))

(def html
  (fetch-url *base-url*))

(def scrape-and-print
  (print-items (scraping/scrape-items html scraper)))