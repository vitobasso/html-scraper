(ns scraper.main-test
  (:require [clojure.test :refer :all]
            [scraper.main :refer :all]))

(deftest test-parse-selector-word
  (testing "Single tag"
    (is (= :div (parse-selector-word "div")))))
