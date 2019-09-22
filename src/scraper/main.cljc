(ns scraper.main)

(defn print-items [items]
  (doseq [item items] (println item)))

(defn ^:export hello [] "hello from html-scraper")

(defn say-hi [] (prn "hi from html-scraper"))