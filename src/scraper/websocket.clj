(ns scraper.websocket
  (:require [clojure.data.json :as json]
            [org.httpkit.server :as w]
            [scraper.helper :as s]))

(def state (atom {}))

(defn list-sources [_]
  {:subject "list-sources",
   :content (s/list-configs)})

(defn map-keys [f map]
  (reduce-kv (fn [m k v] (assoc m (f k) v)) {} map))

(defn change-source [raw-params]
  (let [params (map-keys keyword raw-params)
        source (:name params)]
    (swap! state assoc :source source))
    nil)

(defn convert-item [item]
  (-> item
      (assoc :id (:url item))
      (clojure.set/rename-keys {:url :link, :name :title})))

(defn try-convert-item [item]
  (try (convert-item item)
       (catch Exception _ (prn "Failed: " item))))

(defn items [raw-params]
  (let [params   (map-keys keyword raw-params)
        source   (:source @state)
        keywords (:keywords params)
        page     (Integer. (:page params))
        results   (s/scrape-list source {:search-term keywords :page-number page})]
    {:subject "items",
     :params  raw-params,
     :content (remove nil? (map try-convert-item results))}))

(defn detail [raw-params]
  (let [params   (map-keys keyword raw-params)
        source   (:source @state)
        item     (:item params)
        result   (s/scrape-detail source item)]
    {:subject "detail",
     :params  raw-params,
     :content result}))

(def map-subject
  {:list-sources  list-sources
   :change-source change-source
   :items         items
   :detail        detail})

(defn decode-request [msg]
  (->> msg
       json/read-str
       (map-keys keyword)))

(defn handle-request [request]
  (let [subject (keyword (:subject request))
        params (:params request)
        handler (subject map-subject)]
    (handler params)))

(defn maybe-respond [result channel]
  (if result
    (->> result
         json/write-str
         (w/send! channel))))

(defn receive-message [msg channel]
  (-> msg
      decode-request
      handle-request
      (maybe-respond channel)))

(defn handler [request]
  (w/with-channel request channel
                  (w/on-close channel (fn [status] (println "client close it" status)))
                  (w/on-receive channel (fn [data] (receive-message data channel)))))

(defn -main []
  (w/run-server handler {:port 8080})) ;TODO get port from config


