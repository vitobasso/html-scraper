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
  "https://www.booking.com/searchresults.en-us.html?label=gen173nr-1DCAEoggI46AdIM1gEaFCIAQGYATG4AQfIAQzYAQPoAQH4AQKIAgGoAgO4AtvumusFwAIB&sid=e0eddec745517c5943b68c9173ce7e52&sb=1&src=index&src_elem=sb&error_url=https%3A%2F%2Fwww.booking.com%2Findex.html%3Flabel%3Dgen173nr-1DCAEoggI46AdIM1gEaFCIAQGYATG4AQfIAQzYAQPoAQH4AQKIAgGoAgO4AtvumusFwAIB%3Bsid%3De0eddec745517c5943b68c9173ce7e52%3Bsb_price_type%3Dtotal%26%3B&ss=London%2C+Greater+London%2C+United+Kingdom&is_ski_area=0&checkin_year=2019&checkin_month=8&checkin_monthday=29&checkout_year=2019&checkout_month=8&checkout_monthday=30&group_adults=1&group_children=0&no_rooms=1&b_h4u_keep_filters=&from_sf=1&ss_raw=london&ac_position=0&ac_langcode=en&ac_click_type=b&dest_id=-2601889&dest_type=city&iata=LON&place_id_lat=51.507391&place_id_lon=-0.127634&search_pageview_id=db8079add185008c&search_selected=true")

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

(def items
  (html/select html (:items scraper)))

(def item
  (first items))

(def scrape-and-print
  (print-items (scraping/scrape-items html scraper)))