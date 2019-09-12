(ns scraper.websocket
  (:require [org.httpkit.server :as h])
  (:require [clojure.data.json :as json]
            [scraper.endpoints :as s]))

(def map-subject
  {:list-sources  s/sites
   :items         s/page
   :detail        s/detail})

(defn map-keys [f map]
  (reduce-kv (fn [m k v] (assoc m (f k) v)) {} map))

(defn decode-request [msg]
  (->> msg
       json/read-str
       (map-keys keyword)))

(defn handle-request [request]
  (println request)
  (let [subject (keyword (:subject request))
        args (:content request)
        handler (subject map-subject)]
    (apply handler args)))

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

