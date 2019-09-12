(ns scraper.websocket
  (:require [org.httpkit.server :as h])
  (:require [clojure.data.json :as json]
            [scraper.endpoints :as s]))

(def state (atom {}))

(defn list-sources [param]
  (s/sites))

(defn map-keys [f map]
  (reduce-kv (fn [m k v] (assoc m (f k) v)) {} map))

(defn change-source [raw-params]
  (let [params (map-keys keyword raw-params)
        source (:name params)]
    (swap! state assoc :source source)))

(defn items [raw-params]
  (let [params   (map-keys keyword raw-params)
        source   (:source @state)
        keywords (:keywords params)
        page     (Integer. (:page params))]
    (s/page source keywords page)))

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

(defn receive-message [msg]
  (-> msg
      decode-request
      handle-request
      json/write-str))

(defn handler [request]
  (h/with-channel request channel
                  (h/on-close channel (fn [status] (println "client close it" status)))
                  (h/on-receive channel (fn [data] (h/send! channel (receive-message data))))))

(h/run-server handler {:port 9090}) ;TODO get port from config

