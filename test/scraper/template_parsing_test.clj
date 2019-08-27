(ns scraper.template-parsing-test
  (:require [clojure.test :refer :all]
            [scraper.template-parsing :refer :all]))

(deftest test-parse-selector-word
  (testing "simple tag"
    (is (= :div (parse-selector-word "div"))))
  (testing "tag with class"
    (is (= :div.a-class (parse-selector-word "div.a-class"))))
  (testing "tag with nth-child"
    (let [[tag fun] (parse-selector-word "div:nth-child(1)")]
      (is (= :div tag) (function? fun))))
  )
