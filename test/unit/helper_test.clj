(ns unit.helper-test
  (:require [clojure.test :refer :all]
            [scraper.helper :refer :all]))

(deftest test-derive-paging-params
  (testing "items-per-page and offset can be derived"
    (is (= {:page-number "1" :items-per-page "25" :page-offset "0"}
           (derive-paging-params {:page-number "1"})))
    (is (= {:page-number "3" :items-per-page "25" :page-offset "50"}
           (derive-paging-params {:page-number "3"}))))
  (testing "page-number is optional"
    (is (= {:page-number "1" :items-per-page "25" :page-offset "0"}
           (derive-paging-params {}))))
  (testing "items-per-page is optional"
    (is (= {:page-number "1" :items-per-page "10" :page-offset "0"}
           (derive-paging-params {:items-per-page "10"}))))
  (testing "paging params can be passed as int"
    (is (= {:page-number "2" :items-per-page "10" :page-offset "10"}
           (derive-paging-params {:page-number 2 :items-per-page 10})))))

(deftest test-list-url
  (testing "basic"
    (is (= "http://site.com?k=product&p=1"
           (list-url {:list-page {:url "http://site.com?k=${search-term}&p=${page-number}"}}
                     {:search-term "product" :page-number "1"}))))
  (testing "derived params"
    (is (= "http://site.com?k=product&size=25&offset=50"
           (list-url {:list-page {:url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}"}}
                     {:search-term "product" :page-number "3"}))))
  ;;TODO invalid template
  ;;TODO zero and negative page
  ;;TODO page not parsable to int
  )
