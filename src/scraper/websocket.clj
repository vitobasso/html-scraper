(ns scraper.websocket
  (:require [org.httpkit.server :as h])
  (:require [clojure.data.json :as json]
            [scraper.endpoints :as s]))

(def state (atom {}))

(defn list-sources [_]
  {:subject "list-sources",
   :content (s/sites)})

(defn map-keys [f map]
  (reduce-kv (fn [m k v] (assoc m (f k) v)) {} map))

(defn change-source [raw-params]
  (let [params (map-keys keyword raw-params)
        source (:name params)]
    (swap! state assoc :source source))
    nil)

(defn convert-price [price]
  (let [value (->> price
                 (re-find #"\d+(\.\d+)?")
                 first
                 Double.)]
    {:currency "$",
     :value    value}))

(defn convert-item [item]
  (-> item
      (assoc :id (:url item))
      (update :price convert-price)
      (clojure.set/rename-keys {:url :link, :name :title})))

(defn items [raw-params]
  (let [params   (map-keys keyword raw-params)
        source   (:source @state)
        keywords (:keywords params)
        page     (Integer. (:page params))
        results   (s/page source keywords page)]
    {:subject "items",
     :params  raw-params,
     :content (map convert-item results)}))

(def map-subject
  {:list-sources  list-sources
   :change-source change-source
   :items         items
   :detail        s/detail})

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
         (h/send! channel))))

(defn receive-message [msg channel]
  (-> msg
      decode-request
      handle-request
      (maybe-respond channel)))

(defn handler [request]
  (h/with-channel request channel
                  (h/on-close channel (fn [status] (println "client close it" status)))
                  (h/on-receive channel (fn [data] (receive-message data channel)))))

(h/run-server handler {:port 8080}) ;TODO get port from config

