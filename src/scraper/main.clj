(ns scraper.main
    (:require [scraper.template-parsing :as template-parsing])
    (:require [yaml.core :as yaml])
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* 
  "https://www.amazon.co.uk/s?k=cardboard+boxes&crid=2GG8KOUJMLJRY&sprefix=cardboard%2Caps%2C143&ref=nb_sb_ss_i_1_9")

(defn fetch-url [url]
  (html/html-snippet (:body (client/get url))))

(def template (yaml/from-file "templates/amazon-co-uk.yml"))

(def scraper
  (template-parsing/parse-scraper template))

(defn scrape-field [html field-scraper]
  (let [path (:path field-scraper)
        extract (:extractor field-scraper)]
    {(:name field-scraper) (first (flatten (map extract (html/select html path))))}))

(defn scrape-item [item]
  (map #(scrape-field item %) (:fields scraper)))

(defn scrape-items [html]
  (map scrape-item (html/select html (:items scraper))))

(defn print-items [items]
  (doseq [item items]
    (doseq [field item]
      (println field))))


(def html
  (fetch-url *base-url*))

(def scrape-and-print
  (print-items (scrape-items html)))