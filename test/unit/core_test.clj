(ns unit.core-test
  (:require [clojure.test :refer :all]
            [scraper.core :refer :all]
            [scraper.config :as config]))

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
  (testing "nil on mismatch"
    (is (= nil
           (regex-extract "no numbers" {:find #"(\d+)" :replace "${1}"}))))
  (testing "if more regex groups than replace vars, ignore the extra groups"
    (is (= "750"
           (regex-extract "£750 pcm" {:find #"(\d+([\.,]\d+)?)" :replace "${1}"}))))
  ;;TODO escaped $
  ;;TODO config missing :find or :replace
  )

(deftest test-replace-vars
  (testing "replace two vars"
    (is (= "I love my cat too much"
           (replace-vars "I love my ${pet} ${amount}" {:pet "cat", :amount "too much"}))))
  (testing "wrong keys"
    (is (= "I love my ${pet}"
           (replace-vars "I love my ${pet}" {:human "Abraham Lincoln"}))))
  (testing "key with nil value"
    (is (= "I love my ${pet}"
           (replace-vars "I love my ${pet}" {:human nil}))))
  (testing "nil map"
    (is (= "I love my ${pet}"
           (replace-vars "I love my ${pet}" nil))))
  (testing "if no vars, ignore the keys"
    (is (= "Nobody ain't got time for replacements"
           (replace-vars "Nobody ain't got time for replacements" {:key "put this please"}))))
  (testing "ignore extra keys"
    (is (= "just one key so be it please"
           (replace-vars "just one key ${here} please" {:here "so be it" :there "put this too"})))))

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
           (replace-indexes "${1}" ["oh no, there's a $ in my string"])))))

(deftest test-extract-value
  (testing "handles nil"
    (is (nil? (extract-value (config/parse-property {:select "div"}) nil))))
  (testing "handles string"
    (is (nil? (extract-value (config/parse-property {:select "div"}) ""))))
  (testing "handles has on nil"
    (is (nil? (extract-value (config/parse-property {:select ":has(div)"}) nil))))
  (testing "handles has on string"
    (is (nil? (extract-value (config/parse-property {:select ":has(div)"}) ""))))
  (testing "handles has on nil content"
    (is (nil? (extract-value (config/parse-property {:select ":has(div)"})
                             {:type :element, :attrs nil, :tag :div, :content nil} )))))

(def item-html (parse-html "
<div id='foo'>
  <p class='a-name other-class'>the name</p>
  <p>text in the 2nd p tag</p>
  <img src='image.jpg'/>
  some text
</div>"))

(deftest test-scrape-property
  (testing "class and content"
    (let [src {:name "name", :select ".a-name"}
          config (config/parse-named-property src)]
      (is (= {:name "the name"}
             (scrape-property item-html config)))))
  (testing "id and property"
    (let [src {:name "image", :select "#foo img", :extract "attrs src"}
          config (config/parse-named-property src)]
      (is (= {:image "image.jpg"}
             (scrape-property item-html config)))))
  (testing "text with element siblings"
    (let [src {:name "name", :select "#foo"}
          config (config/parse-named-property src)]
      (is (= {:name "some text"}
             (scrape-property item-html config)))))
  (testing "matching regex"
    (let [src {:name "name", :select "p", :regex {:find "(.*2nd.*)"}}
          config (config/parse-named-property src)]
      (is (= {:name "text in the 2nd p tag"}
             (scrape-property item-html config)))))
  (testing "matching regex fails"
    (let [src {:name "name", :select "p", :regex {:find "wont match this"}}
          config (config/parse-named-property src)]
      (is (= {:name nil}
             (scrape-property item-html config)))))
  (testing "match and replace regex"
    (let [src {:name "name", :select "p", :regex {:find "text in the (.+)", :replace "${1}"}}
          config (config/parse-named-property src)]
      (is (= {:name "2nd p tag"}
             (scrape-property item-html config)))))
  (testing "empty if nil config"
    (let [config nil]
      (is (= {}
             (scrape-property item-html config))))))

(def property-table-html (parse-html "
<table id='properties'>
  <tr>
    <td class='label'>Ram</td>
    <td>16GB</td>
  </tr>
  <tr>
    <td class='label'>Disk</td>
    <td>1TB</td>
  </tr>
</div>"))

(deftest test-scrape-property-table
  (testing "happy case"
    (let [src {:pair-select "#properties tr",
               :label {:select "td.label"},
               :value {:select "td.label + td"}}
          config (config/parse-property-table src)]
      (is (= {:Ram "16GB", :Disk "1TB"}
             (scrape-property-table property-table-html config)))))
  (testing "nil values"
    (let [src {:pair-select "#properties tr",
               :label {:select "td.label"},
               :value {:select "td.absentclass"}}
          config (config/parse-property-table src)]
      (is (= {:Ram nil, :Disk nil}
             (scrape-property-table property-table-html config)))))
  (testing "skip empty labels"
    (let [src {:pair-select "#properties tr",
               :label {:select "td.absentclass"},
               :value {:select "td.label + td"}}
          config (config/parse-property-table src)]
      (is (= {}
             (scrape-property-table property-table-html config)))))
  (testing "empty if nil config"
    (let [config nil]
      (is (= {}
             (scrape-property-table property-table-html config))))))


(deftest test-scrape-item
  (testing "config having properties"
    (let [src {:properties  [{:name "label", :select ".label"}]}
          config (config/parse-detail-page src)]
      (is (= {:label "Ram"}
             (scrape-item property-table-html config)))))
  (testing "config having property-tables"
      (let [src {:property-tables
                   [{:pair-select "#properties tr",
                    :label {:select "td.label"},
                    :value {:select "td.label + td"}}]}
            config (config/parse-detail-page src)]
        (is (= {:Ram "16GB", :Disk "1TB"}
               (scrape-item property-table-html config)))))
  (testing "config having both properties and property-tables"
      (let [src {:properties  [{:name "label", :select ".label"}]
                 :property-tables
                   [{:pair-select "#properties tr",
                    :label {:select "td.label"},
                    :value {:select "td.label + td"}}]}
            config (config/parse-detail-page src)]
        (is (= {:label "Ram" :Ram "16GB", :Disk "1TB"}
               (scrape-item property-table-html config))))))

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
  (testing "item select"
    (let [src {:item-select "p"
               :properties  [{:name "name", :select "span"} ;TODO rm span, use root selector?
                             {:name "id", :select "p", :extract "attrs id"}]}
          config (config/parse-list-page src)]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"}]
             (scrape-items list-html config)))))
  (testing "item split"
    (let [src {:container-select "div" :item-split "(?=<p)"
               :properties  [{:name "name", :select "span"}
                             {:name "id", :select "p", :extract "attrs id"}]}
          config (config/parse-list-page src)]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"}]
             (scrape-items list-html config)))))
  (testing "item split with two containers"
    (let [src {:container-select "div" :item-split "(?=<p)"
               :properties  [{:name "name", :select "span"}
                             {:name "id", :select "p", :extract "attrs id"}]}
          config (config/parse-list-page src)]
      (is (= [{:name "item 1", :id "a"} {:name "item 2", :id "b"} {:name "item 3", :id "c"} {:name "item 4", :id "d"}]
             (scrape-items two-column-html config))))))