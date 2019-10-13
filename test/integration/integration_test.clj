(ns integration.integration-test
  (:require [scraper.scraping :as scr])
  (:require [scraper.config :as cfg])
  (:require [clojure.test :refer :all]))

(deftest preconditions
  (testing "internet connection"
    (is (some? (slurp "http://google.com")))))

(deftest integration-test
  (let [state (atom {})] ;the result of each test will be used by the next

  (testing "config"
    (let [config (cfg/load-config "amazon")]
      (is (not (nil? config)))
      (swap! state assoc :config config)))

  (testing "list"
    (let [list (scr/scrape-list (:config @state) {:search-term "cardboard boxes" :page-number "1"})]
      (are [x] (true? x)
        (-> list count (> 10))
        (-> list first :name string?)
        (->> list first :url (re-find #"/.*") some?)
        (->> list first :image (re-find #"https://.*\.jpg") some?)
        (->> list first :price (re-find #"£\d+") some?))
      (swap! state assoc :item (first list))))

  (testing "details"
    (let [details (scr/scrape-detail (:config @state) (:item @state))]
      (is (-> details count (> 10)))))))