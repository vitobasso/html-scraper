(ns scraper.template-parsing-test
  (:require [clojure.test :refer :all]
            [scraper.template-parsing :refer :all]))

(deftest test-parse-selector-word
  (testing "single tag"
    (is (= :div (parse-selector-word "div"))))
  (testing "tag with nth-child"
    (let [[tag fun] (parse-selector-word "div:nth-child(1)")]
      (is (= :div tag) (function? fun))))
  )
