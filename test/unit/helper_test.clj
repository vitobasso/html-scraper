(ns unit.helper-test
  (:require [clojure.test :refer :all]
            [scraper.helper :refer :all]))

(deftest test-build-search-url
  (testing "page-number"
    (is (= "http://site.com?k=product&p=1"
           (build-search-url "http://site.com?k=${search-term}&p=${page-number}"
                             {:search-term "product" :page-number "1"}))))
  (testing "items-per-page and offset can be derived"
    (is (= "http://site.com?k=product&size=25&offset=0"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"
                             {:search-term "product" :page-number "1"})))
    (is (= "http://site.com?k=product&size=25&offset=50"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"
                             {:search-term "product" :page-number "3"}))))
  (testing "page-number is optional"
    (is (= "http://site.com?k=product&size=25&offset=0"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"
                             {:search-term "product"})))
    (is (= "http://site.com?k=product&p=1"
           (build-search-url "http://site.com?k=${search-term}&p=${page-number}"
                             {:search-term "product"}))))
  (testing "items-per-page is optional"
    (is (= "http://site.com?k=product&size=10&offset=0"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"
                             {:search-term "product" :items-per-page "10"}))))
  (testing "paging params can be passed as int"
    (is (= "http://site.com?k=product&size=10&offset=10"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"
                             {:search-term "product" :page-number 2 :items-per-page 10}))))
  ;;TODO invalid template
  ;;TODO zero and negative page
  ;;TODO page not parseable to int
  )
