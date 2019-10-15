(ns scraper.helper
  (:require [scraper.core :as s])
  (:require [scraper.config :as c])
  (:require [yaml.core :as yaml])
  (:require [clj-http.client :as client]))

(defn extract-site-name [file-name]
  (let [[_ site-name] (re-find #"(.*)\.ya?ml" file-name)]
    site-name))

(defn list-configs []
  (->> (clojure.java.io/file "templates")
       .listFiles
       (map #(.getName %))
       (map extract-site-name)
       (remove nil?)))

(defn load-config [name]
  (-> (str "templates/" name ".yml")
      yaml/from-file
      c/parse-config))

(defn derive-paging-params [given-params]
  (let [default-page-number "1"
        default-items-per-page "25" ;; TODO get default from config
        page-number (or (:page-number given-params) default-page-number)
        items-per-page (or (:items-per-page given-params) default-items-per-page)
        page-offset (or (:page-offset given-params) (* (- (bigint page-number) 1) (bigint items-per-page)))]
    (merge given-params
           {:page-number    (str page-number)
            :items-per-page (str items-per-page)
            :page-offset    (str page-offset)})))

(defn build-search-url [url-template params]
  (s/replace-vars url-template (derive-paging-params params)))

(defn list-url [config params]
  (let [url-template (-> config :list-page :url)]
    (build-search-url url-template params)))

(defn detail-url [config item-kv]
  (let [url-template (-> config :detail-page :url)]
    (s/replace-vars url-template item-kv)))

(defn scrape-list [site-name params]
  (let [config (load-config site-name)
        url (list-url config params)
        html (-> url client/get :body)]
    (prn "scrape-list: " url)
    (s/scrape-list config html)))

(defn scrape-detail [site-name item-kv]
  (let [config (load-config site-name)
        url (detail-url config item-kv)
        html (-> url client/get :body)]
    (prn "scrape-detail: " url)
    (s/scrape-detail config html)))