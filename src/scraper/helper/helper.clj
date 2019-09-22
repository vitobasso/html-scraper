(ns scraper.helper.helper
  (:require [scraper.core :as s])
  (:require [scraper.config :as c])
  (:require [scraper.helper.common :as h])
  (:require [yaml.core :as yaml])
  (:require [clj-http.client :as http]))

(defn list-configs []
  (->> (clojure.java.io/file "templates")
       .listFiles
       (map #(.getName %))
       (map h/path->name)
       (remove nil?)))

(defn load-config [name]
  (-> (str "templates/" name ".yml")
      yaml/from-file
      c/parse-config))

(defn scrape-list [site-name params]
  (let [config (load-config site-name)
        url (h/list-url config params)
        html (-> url http/get :body)]
    (prn "scrape-list: " url)
    (s/scrape-list config html)))

(defn scrape-detail [site-name item-kv]
  (let [config (load-config site-name)
        url (h/detail-url config item-kv)
        html (-> url http/get :body)]
    (prn "scrape-detail: " url)
    (s/scrape-detail config html)))