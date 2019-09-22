(ns scraper.helper.common
  (:require [scraper.core :as s]
            [clojure.edn :as edn]))

(defn path->name [path]
  (let [[_ name] (re-find #"(\S+)\.ya?ml" path)]
    name))

(defn name->path [name]
  (str name ".yml"))

(defn parsed [val]
  (edn/read-string (str val)))

(defn derive-paging-params [given-params]
  (let [default-page-number "1"
        default-items-per-page "25" ;; TODO get default from config
        page-number (or (:page-number given-params) default-page-number)
        items-per-page (or (:items-per-page given-params) default-items-per-page)
        page-offset (or (:page-offset given-params)
                        (* (- (parsed page-number) 1) (parsed items-per-page)))]
    (merge given-params
           {:page-number    (str page-number)
            :items-per-page (str items-per-page)
            :page-offset    (str page-offset)})))

(defn list-url [config params]
  (let [url-template (-> config :list-page :url)
        derived-params (derive-paging-params params)]
    (s/replace-vars url-template derived-params)))

(defn detail-url [config item-kv]
  (let [url-template (-> config :detail-page :url)]
    (s/replace-vars url-template item-kv)))
