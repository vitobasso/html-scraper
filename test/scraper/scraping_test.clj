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
           (regex-extract "it costs 10 pounds" {:find #"\D*(\d+)\D*", :replace "£${1}"}))))
  (testing "extract two things"
    (is (= "£10.50"
           (regex-extract "it costs 10 pounds and 50 pence" {:find #"\D*(\d+)\D*(\d+)\D*", :replace "£${1}.${2}"}))))
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
           (replace-vars "I love my ${pet} ${amount}" {:pet "cat", :amount "too much"}))))
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
  (testing "value may contain a $"
    (is (= "oh no, there's a $ in my string"
           (replace-indexes "${1}" ["oh no, there's a $ in my string"]))))
  )

(def item-html (parse-html "
<div id='foo'>
  <p class='a-name other-class'>the name</p>
  <p>text in the 2nd p tag</p>
  <img src='image.jpg'/>
  some text
</div>"))

(deftest test-scrape-attribute
  (testing "class and content"
    (let [src {:name "name", :selector ".a-name", :extractor "content"}
          config (config/parse-named-attribute src)]
      (is (= {:name "the name"}
             (scrape-attribute item-html config)))))
  (testing "id and attribute"
    (let [src {:name "image", :selector "#foo img", :extractor "attrs src"}
          config (config/parse-named-attribute src)]
      (is (= {:image "image.jpg"}
             (scrape-attribute item-html config)))))
  (testing "text with element siblings"
    (let [src {:name "name", :selector "#foo", :extractor "content"}
          config (config/parse-named-attribute src)]
      (is (= {:name "some text"}
             (scrape-attribute item-html config)))))
  (testing "matching regex"
    (let [src {:name "name", :selector "p", :extractor "content", :regex {:find "(.*2nd.*)"}}
          config (config/parse-named-attribute src)]
      (is (= {:name "text in the 2nd p tag"}
             (scrape-attribute item-html config)))))
  (testing "matching regex fails"
    (let [src {:name "name", :selector "p", :extractor "content", :regex {:find "wont match this"}}
          config (config/parse-named-attribute src)]
      (is (= {:name nil}
             (scrape-attribute item-html config)))))
  (testing "match and replace regex"
    (let [src {:name "name", :selector "p", :extractor "content", :regex {:find "text in the (.+)", :replace "${1}"}}
          config (config/parse-named-attribute src)]
      (is (= {:name "2nd p tag"}
             (scrape-attribute item-html config)))))
  (testing "empty if nil config"
    (let [config nil]
      (is (= {}
             (scrape-attribute item-html config))))))

(def attribute-table-html (parse-html "
<table id='attributes'>
  <tr>
    <td class='label'>Ram</td>
    <td>16GB</td>
  </tr>
  <tr>
    <td class='label'>Disk</td>
    <td>1TB</td>
  </tr>
</div>"))

(deftest test-scrape-attribute-table
  (testing "attribute table"
    (let [src {:selector "#attributes tr",
               :label {:selector "td.label", :extractor "content"},
               :value {:selector "td.label + td", :extractor "content"}}
          config (config/parse-attribute-table src)]
      (is (= {:Ram "16GB", :Disk "1TB"}
             (scrape-attribute-table attribute-table-html config)))))
  (testing "empty if nil config"
    (let [config nil]
      (is (= {}
             (scrape-attribute-table attribute-table-html config))))))


(deftest test-scrape-item
  (testing "config having attributes"
    (let [src {:attributes  [{:name "label", :selector ".label", :extractor "content"}]}
          config (config/parse-detail-page src "dummy")]
      (is (= {:label "Ram"}
             (scrape-item attribute-table-html config)))))
  (testing "config having attribute-table"
      (let [src {:attribute-table
                   {:selector "#attributes tr",
                    :label {:selector "td.label", :extractor "content"},
                    :value {:selector "td.label + td", :extractor "content"}}}
            config (config/parse-detail-page src "dummy")]
        (is (= {:Ram "16GB", :Disk "1TB"}
               (scrape-item attribute-table-html config)))))
  (testing "config having both attributes and attribute-table"
      (let [src {:attributes  [{:name "label", :selector ".label", :extractor "content"}]
                 :attribute-table
                   {:selector "#attributes tr",
                    :label {:selector "td.label", :extractor "content"},
                    :value {:selector "td.label + td", :extractor "content"}}}
            config (config/parse-detail-page src "dummy")]
        (is (= {:label "Ram" :Ram "16GB", :Disk "1TB"}
               (scrape-item attribute-table-html config))))))

(def list-html (parse-html "
<div>
  <p id='a'><span>item 1</span></p>
  <p id='b'><span>item 2</span></p>
  <p id='c'><span>item 3</span></p>
</div>"))

(def two-column-html (parse-html "
<div>
  <p id='a'><span>item 1</span></p>
  <p id='b'><span>item 2</span></p>
</div>
<div>
  <p id='c'><span>item 3</span></p>
  <p id='d'><span>item 4</span></p>
</div>"))

(deftest test-scrape-items
  (testing "item selector"
    (let [src {:item-selector "p"
               :attributes  [{:name "name", :selector "span", :extractor "content"} ;TODO rm span, use root selector?
                             {:name "id", :selector "p", :extractor "attrs id"}]}
          config (config/parse-list-page src "dummy")]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"}]
             (scrape-items list-html config)))))
  (testing "item separator"
    (let [src {:container-selector "div" :item-separator "(?=<p)"
               :attributes  [{:name "name", :selector "span", :extractor "content"}
                             {:name "id", :selector "p", :extractor "attrs id"}]}
          config (config/parse-list-page src "dummy")]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"}]
             (scrape-items list-html config)))))
  (testing "item separator with two containers"
    (let [src {:container-selector "div" :item-separator "(?=<p)"
               :attributes  [{:name "name", :selector "span", :extractor "content"}
                             {:name "id", :selector "p", :extractor "attrs id"}]}
          config (config/parse-list-page src "dummy")]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"} {:name "item 4", :id "d"}]
             (scrape-items two-column-html config))))))

;TODO move non websocket commits to master