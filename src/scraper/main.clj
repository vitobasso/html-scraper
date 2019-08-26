(ns scraper.main
    (:require [yaml.core :as yaml])
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* 
  "https://www.amazon.co.uk/s?k=cardboard+boxes&crid=2GG8KOUJMLJRY&sprefix=cardboard%2Caps%2C143&ref=nb_sb_ss_i_1_9")

(defn fetch-url [url]
  (html/html-snippet (:body (client/get url))))

(def template (yaml/from-file "templates/amazon-co-uk.yml"))

(defn split-by-space [str]
  (clojure.string/split str #" "))

(defn parse-selector-word [str] ;; TODO handle tag.class (intersection) and other tag:function
  (let [[matches tag num] (re-find #"(.*):nth-child\((\d+)\)" str)]
    (if matches
      [(keyword tag) (html/nth-child (Integer/parseInt num))]
      (keyword str))))

(defn parse-extractor [str]
  (apply comp (reverse (map keyword (split-by-space str)))))

(defn parse-selector [str]
  (into [] (map parse-selector-word (split-by-space str))))

(defn parse-field-scraper [selector-def]
  {:name (:name selector-def)
   :path (parse-selector (:path selector-def))
   :extractor (parse-extractor (:extractor selector-def))})

(defn parse-scraper [template]
  {:items (parse-selector (:items template))
   :fields (map parse-field-scraper (:fields template))})

(def scraper
  (parse-scraper template))

(defn scrape-field [html field-scraper]
  (let [path (:path field-scraper)
        extract (:extractor field-scraper)]
    {(:name field-scraper) (first (flatten (map extract (html/select html path))))}))

(defn scrape-item [item]
  (map #(scrape-field item %) (:fields scraper)))

(defn scrape-items [html]
  (map scrape-item (html/select html (:items scraper))))


;; draft

(def html
  (fetch-url *base-url*))

(defn parseit []
  (scrape-items html))

(def printit
  (doseq [item (parseit)] (doseq [field item] (println field))))