(ns integration.integration-test
  (:require [clojure.test :refer :all])
  (:require [scraper.core :as s])
  (:require [scraper.config :as c])
  (:require [scraper.helper :as h])
  (:require [yaml.core :as yaml])
  (:require [clj-http.client :as http]))

(deftest ^:remote preconditions
  (testing "internet connection"
    (is (some? (slurp "http://google.com")))))

(defn- load-config [name]
  (-> (str "templates/" name ".yml")
      yaml/from-file
      c/parse-config))

(defn- scrape-list [site-name params]
  (let [config (load-config site-name)
        url (h/list-url config params)
        html (-> url http/get :body)]
    (s/scrape-list config html)))

(defn- scrape-detail [site-name item-kv]
  (let [config (load-config site-name)
        url (h/detail-url config item-kv)
        html (-> url http/get :body)]
    (s/scrape-detail config html)))

(deftest ^:remote integration-test
  (let [state (atom {})] ;the result of one test will be used by the other

  (testing "list"
    (let [list (scrape-list "amazon" {:search-term "cardboard boxes" :page-number "1"})]
      (are [x] (true? x)
        (-> list count (> 10))
        (-> list first :name string?)
        (->> list first :url (re-find #"/.*") some?)
        (->> list first :image (re-find #"https://.*\.jpg") some?)
        (->> list first :price (re-find #"£\d+") some?))
      (swap! state assoc :item (first list))))

  (testing "details"
    (let [details (scrape-detail "amazon" (:item @state))]
      (is (-> details count (> 10)))))))