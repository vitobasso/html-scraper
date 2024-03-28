(defproject html-scraper "0.1.0"
  :description "HTML Scraper"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [hickory "0.7.1"]
                 [com.github.vitobasso/hickory-css-selector "custom-for-papershop-SNAPSHOT"]
                 [io.forward/yaml "1.0.11"]
                 [clj-http "3.10.0"]]
  :repositories {"jitpack" {:url "https://jitpack.io"}}
  :test-selectors {:default (complement :remote)
                   :integration :remote
                   :all (constantly true)})
