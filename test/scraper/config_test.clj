(ns scraper.config-test
  (:require [clojure.test :refer :all]
            [scraper.config :refer :all]))

(def config-src
  {:home-url "https://www.page.com"
   :list-page {
     :url-path "/searchresults.html"
     :item-select "#search_results .item"}
   :detail-page {
     :url-path "${url}"}})

(def parsed-config
  (parse-config config-src))

(deftest test-parse-config
  (testing "home url"
    (is (= "https://www.page.com"
           (:home-url parsed-config))))
  (testing "list url"
    (is (= "https://www.page.com/searchresults.html"
           (:url (:list-page parsed-config)))))
  (testing "detail url"
    (is (= "https://www.page.com${url}"
           (:url (:detail-page parsed-config)))))
  )

(deftest test-item-selection
  (testing "item-select happy case"
    (is (some? (parse-config {:home-url "https://www.page.com"
                              :list-page {:url-path "/searchresults.html"
                                          :item-select "#search_results .item"}}))))
  (testing "item-split happy case"
    (is (some? (parse-config {:home-url "https://www.page.com"
                              :list-page {:url-path "/searchresults.html"
                                          :container-select "#search_results .item"
                                          :item-split "regex"}}))))
  (testing "fails when missing item-split"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :container-select "#search_results .item"}}))))
  (testing "fails when missing container-select"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :item-split "regex"}}))))
  (testing "fails when missing all item select params"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"}}))))
  (testing "fails when has item-select and container-select at the same time"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :container-select "#search_results .item"
                                            :item-select "#search_results .item"}}))))
  )

;TODO fail: invalid property/regex/find
