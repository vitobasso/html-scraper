(ns scraper.scraping-test
  (:require [clojure.test :refer :all]
            [scraper.scraping :refer :all]))

(deftest test-interpolate-url
  (testing "page number"
    (is (= "http://site.com?k=product&p=1"
           (interpolate-url "http://site.com?k=${SEARCH_TERM}&p=${PAGE_NUMBER}" "product" 1))))
  (testing "items per page and offset, first page"
    (is (= "http://site.com?k=product&size=25&offset=0"
           (interpolate-url "http://site.com?k=${SEARCH_TERM}&size=${ITEMS_PER_PAGE}&offset=${PAGE_OFFSET}" "product" 1))))
  (testing "items per page and offset, third page"
    (is (= "http://site.com?k=product&size=25&offset=50"
           (interpolate-url "http://site.com?k=${SEARCH_TERM}&size=${ITEMS_PER_PAGE}&offset=${PAGE_OFFSET}" "product" 3))))
  ;;TODO invalid template
  ;;TODO zero and negative page
  ;;TODO non int page
  )
