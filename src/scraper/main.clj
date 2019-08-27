(ns scraper.main
    (:require [scraper.scraping :as scraping])
    (:require [yaml.core :as yaml])
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html]))

(def template (yaml/from-file "templates/amazon-co-uk.yml"))

(def ^:dynamic *base-url*
  "https://www.amazon.co.uk/s?k=cardboard+boxes&crid=2GG8KOUJMLJRY&sprefix=cardboard%2Caps%2C143&ref=nb_sb_ss_i_1_9")

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