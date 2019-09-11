(ns scraper.websocket
  (:require [org.httpkit.server :as h])
  (:require [clojure.data.json :as json]
            [scraper.endpoints :as s]))

(def map-command
  {:sites  s/sites
   :page   s/page
   :detail s/detail})

(defn decode-request [msg]
  (-> msg
      json/read-str
      (update 0 keyword)))

(defn handle-command [[cmd & args]]
  (apply (cmd map-command) args))

(defn receive-msg [msg]
  (-> msg
      decode-request
      handle-command
      json/write-str))

(defn handler [request]
  (h/with-channel request channel
                  (h/on-close channel (fn [status] (println "client close it" status)))
                  (h/on-receive channel (fn [data] (h/send! channel (receive-msg data))))))

(h/run-server handler {:port 9090}) ;TODO get port from config

