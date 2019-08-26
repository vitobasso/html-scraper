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

(defn parse-selector-word [str]
  (let [[matches tag num] (re-find #"(.*):nth-child\((\d+)\)" str)]
    (if matches
      [(keyword tag) (html/nth-of-type num)]
      (keyword str))))

(defn parse-extractor [str]
  (comp (reverse (map keyword (split-by-space str)))))

(defn parse-selector [selector-def]
  {:name (:name selector-def)
   :select-keys (map parse-selector-word (split-by-space (str (:items template) " " (:path selector-def))))
   :extract-fn (parse-extractor (:extractor selector-def))})

(def selectors
  (map parse-selector (:fields template)))

(defn parse-field [html selector]
  (html/select html (:select-keys selector)))


;; draft

(defn fetchit []
  (fetch-url *base-url*))

(def items (html/select html [:.s-search-results :> :div]))

(def item (first items))

(defn parseit []
  (let [html fetchit
        sel selectors]
    (parse-field html (first sel))))
