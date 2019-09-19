(ns scraper.config-test
  (:require [clojure.test :refer :all]
            [scraper.config :refer :all]))

(def config-src
  {:home-url "https://www.page.com"
   :list-page {
     :url-path "/searchresults.html"
     :item-selector "#search_results .item"}
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
  (testing "item-selector happy case"
    (is (some? (parse-config {:home-url "https://www.page.com"
                              :list-page {:url-path "/searchresults.html"
                                          :item-selector "#search_results .item"}}))))
  (testing "item-separator happy case"
    (is (some? (parse-config {:home-url "https://www.page.com"
                              :list-page {:url-path "/searchresults.html"
                                          :container-selector "#search_results .item"
                                          :item-separator "regex"}}))))
  (testing "fails when missing item-separator"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :container-selector "#search_results .item"}}))))
  (testing "fails when missing container-selector"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :item-separator "regex"}}))))
  (testing "fails when missing all item selector params"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"}}))))
  (testing "fails when has item-selector and container-selector at the same time"
    (is (thrown? Exception
                 (parse-config {:home-url "https://www.page.com"
                                :list-page {:url-path "/searchresults.html"
                                            :container-selector "#search_results .item"
                                            :item-selector "#search_results .item"}}))))
  )

;TODO fail: invalid attribute/regex/find
