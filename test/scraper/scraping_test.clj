(ns scraper.scraping-test
  (:require [clojure.test :refer :all]
            [scraper.scraping :refer :all]
            [scraper.config :as config]))

(deftest test-build-search-url
  (testing "page number"
    (is (= "http://site.com?k=product&p=1"
           (build-search-url "http://site.com?k=${search-term}&p=${page-number}" "product" 1))))
  (testing "items per page and offset, first page"
    (is (= "http://site.com?k=product&size=25&offset=0"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}" "product" 1))))
  (testing "items per page and offset, third page"
    (is (= "http://site.com?k=product&size=25&offset=50"
           (build-search-url "http://site.com?k=${search-term}&size=${items-per-page}&offset=${page-offset}" "product" 3))))
  ;;TODO invalid template
  ;;TODO zero and negative page
  ;;TODO non int page
  )

(deftest test-regex-extract
  (testing "extract one thing"
    (is (= "£10"
           (regex-extract "it costs 10 pounds" {:find "\\D*(\\d+)\\D*" :replace "£${1}"}))))
  (testing "extract two things"
    (is (= "£10.50"
           (regex-extract "it costs 10 pounds and 50 pence" {:find "\\D*(\\d+)\\D*(\\d+)\\D*" :replace "£${1}.${2}"}))))
  (testing "if no config, bypass"
    (is (= "bla bla"
           (regex-extract "bla bla" nil))))
  ;;TODO mismatch
  ;;TODO escaped $
  ;;TODO config missing :find or :replace
  )

(deftest test-replace-vars
  (testing "replace two vars"
    (is (= "I love my cat too much"
           (replace-vars "I love my ${pet} ${amount}" {:pet "cat" :amount "too much"}))))
  (testing "if wrong keys, do nothing"
    (is (= "I love my ${pet}"
           (replace-vars "I love my ${pet}" {:human "Abraham Lincoln"}))))
  ;;TODO nil config
  )

(deftest test-replace-indexes
  (testing "replace two place holders"
    (is (= "I love my cat and my other cat"
           (replace-indexes "I love my ${1} and my ${2}" ["cat" "other cat"]))))
  (testing "if missing values, replace as far as possible"
    (is (= "I love my cat and my ${2}"
           (replace-indexes "I love my ${1} and my ${2}" ["cat"]))))
  (testing "if nil values, do nothing"
    (is (= "I love my ${1} and my ${2}"
           (replace-indexes "I love my ${1} and my ${2}" nil))))
  )

(def item-html (parse-html "
<div id='foo'>
  <p class='a-name other-class'>the name</p>
  <img src='image.jpg'/>
  some text
</div>"))

(deftest test-scrape-attribute
  (testing "class and content"
    (let [src {:name "name" :selector ".a-name" :extractor "content"}
          config (config/parse-attribute src)]
      (is (= {:name "the name"}
             (scrape-attribute item-html config)))))
  (testing "id and attribute"
    (let [src {:name "image" :selector "#foo img" :extractor "attrs src"}
          config (config/parse-attribute src)]
      (is (= {:image "image.jpg"}
             (scrape-attribute item-html config)))))
  (testing "text along elements"
    (let [src {:name "name" :selector "#foo" :extractor "content"}
          config (config/parse-attribute src)]
      (is (= {:name "some text"}
             (scrape-attribute item-html config))))))

(def page-html (parse-html "
<div>
  <p><span>item 1</span></p>
  <p><span>item 2</span></p>
  <p><span>item 3</span></p>
</div>"))

(deftest test-scrape-items
  (testing "class and content"
    (let [src {:item-selector "p"
               :attributes  [{:name "name" :selector "span" :extractor "content"}]}
          config (config/parse-list-page src "dummy")]
      (is (= [{:name "item 1"} {:name "item 2"} {:name "item 3"}]
             (scrape-items page-html config))))))
