(ns scraper.helper.helper
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [scraper.core :as s]
            [scraper.config :as c]
            [scraper.helper.github-files :as f]
            [scraper.helper.common :as h]
            [cljs-http.client :as http]
            ["js-yaml" :as yaml]
            [cljs.core.async :refer [<! take!]]))

(defn list-configs []
  (go (->> (<! (f/dir))
           keys
           (map name)
           (map h/path->name))))

(defn hi [callback]
  (take! (go "hi") callback))

(defn parse-config [file]
  (-> file
      yaml/safeLoad
      (js->clj :keywordize-keys true)
      c/parse-config))

(defn load-config [name]
  (let [path (h/name->path name)]
    (go (-> (<! (f/file path))
            parse-config))))

(defn scrape-list [site-name params]
  (go (let [config (<! (load-config site-name))
            url (h/list-url config params)
            response (<! (http/get url {:with-credentials? false})) ;FIXME :status 0, :success false, :body ""
            html (:body response)]
        (prn "scrape-list: " url)
        (s/scrape-list config html))))

(defn scrape-detail [site-name item-kv]
  (go (let [config (<! (load-config site-name))
            url (h/detail-url config item-kv)
            response (<! (http/get url {:with-credentials? false}))
            html (:body response)]
        (prn "scrape-detail: " url)
        (s/scrape-detail config html))))