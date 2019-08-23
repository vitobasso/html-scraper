(ns scraper.main
    (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* "https://www.amazon.co.uk/s?k=cardboard+boxes&crid=2GG8KOUJMLJRY&sprefix=cardboard%2Caps%2C143&ref=nb_sb_ss_i_1_9")

(defn fetch-url [url]
    (html/html-resource (java.net.URL. url)))

(defn fetchit []
    (fetch-url *base-url*))

