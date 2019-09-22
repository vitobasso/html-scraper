(ns integration.integration-test
  (:require [scraper.helper.helper :as s])
  (:require [clojure.test :refer :all]))

(deftest ^:remote preconditions
  (testing "internet connection"
    (is (some? (slurp "http://google.com")))))

(deftest ^:remote integration-test
  (let [state (atom {})] ;the result of one test will be used by the other

  (testing "list"
    (let [list (s/scrape-list "amazon" {:search-term "cardboard boxes" :page-number "1"})]
      (are [x] (true? x)
        (-> list count (> 10))
        (-> list first :name string?)
        (->> list first :url (re-find #"/.*") some?)
        (->> list first :image (re-find #"https://.*\.jpg") some?)
        (->> list first :price (re-find #"£\d+") some?))
      (swap! state assoc :item (first list))))

  (testing "details"
    (let [details (s/scrape-detail "amazon" (:item @state))]
      (is (-> details count (> 10)))))))