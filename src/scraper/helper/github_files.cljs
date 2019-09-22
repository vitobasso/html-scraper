(ns scraper.helper.github-files
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [goog.crypt.base64 :as base64]
            [cljs.core.async :refer [<!]]))

(defn parse-dir-entry [file]
  (let [key (-> file :path keyword)
        val (:url file)]
    {key val}))

(defn parse-dir [response]
  (->> response :body :tree
       (map parse-dir-entry)
       (into {})
       (filter key)))

(defn fetch-dir []
  (go (let [url "https://api.github.com/repos/vitobasso/html-scraper/git/trees/209da9c3dc1a32930865de131344ed5074e4a871"
            response (<! (http/get url {:with-credentials? false}))]
        (parse-dir response))))

(def dir-cache (atom nil))

(defn dir []
  (go (or @dir-cache
          (swap! dir-cache merge (<! (fetch-dir))))))

(defn file-url [path]
  (go (let [key (keyword path)
            url-map (<! (dir))]
        (key url-map))))

(defn parse-file [response]
  (-> response :body :content
      base64/decodeString))

(defn fetch-file [path]
  (go (let [url (<! (file-url path))
            response (<! (http/get url {:with-credentials? false}))]
        (parse-file response))))

(def file-cache (atom nil))

(defn update-file-cache [path]
  (go (swap! file-cache merge
             {(keyword path) (<! (fetch-file path))})))

(defn file [path]
  (go ((keyword path) (or @file-cache
                          (<! (update-file-cache path))))))
