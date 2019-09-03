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
