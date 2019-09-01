(ns scraper.scraping
    (:require [scraper.template-parsing :as template-parsing])
    (:require [net.cgrand.enlive-html :as html])
    (:require [clojure.string :as string])
    (:require [clj-http.client :as client]))

(defn parse-scraper [template]
  (template-parsing/parse-config template))

(defn trim-value [maybe-value]
  (if (some? maybe-value) (string/trim maybe-value) maybe-value))

(defn replace-var [str-template [key value]]
  (let [pattern (re-pattern (str "\\$\\{" (name key) "\\}"))]
    (string/replace str-template pattern value)))

(defn replace-vars [str-template var-map]
  (reduce replace-var str-template var-map))

(defn replace-indexes [str-template values]
  (let [into-key (comp keyword str)
        counting-keys (map into-key (rest (range)))
        var-map (zipmap counting-keys values)]
    (replace-vars str-template var-map)))

(defn regex-extract [value config]
  (if config
    (let [pattern (re-pattern (:find config))
          [_ & groups] (re-find pattern value)
          template (:replace config)]
      (replace-indexes template groups))
    value))

(defn scrape-attribute [full-html config]
  (let [item-html (html/select full-html (:selector config))
        value (-> (map (:extractor config) item-html)
                  (flatten)
                  (first)
                  (trim-value)
                  (regex-extract (:regex config)))]
    {(:name config) value}))

(defn scrape-item [item config]
  (map #(scrape-attribute item %) (:attributes config)))

(defn scrape-items [html config]
  (map #(scrape-item % config) (html/select html (:item-selector config))))

(defn build-search-url [url-template search-term page-number]
  (let [items-per-page 25 ;; TODO get from config
        page-offset (* (- page-number 1) items-per-page)
        vars {"SEARCH_TERM" search-term
              "PAGE_NUMBER" (str page-number)
              "ITEMS_PER_PAGE" (str items-per-page)
              "PAGE_OFFSET" (str page-offset)}]
    (replace-vars url-template vars)))

(defn scrape-list [search-term page-number config]
  (let [list-config (:list-page config)
        url-template (:url list-config)
        url (build-search-url url-template search-term page-number)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-items parsed-html list-config)))

(defn scrape-detail [item config]
  (let [detail-config (:detail-page config)
        url-template (str (:home-url config) (:url-path detail-config))
        url (replace-vars url-template item)
        response-body (:body (client/get url))
        parsed-html (html/html-snippet response-body)]
    (scrape-item parsed-html detail-config)))