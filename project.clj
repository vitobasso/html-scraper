(defproject html-scraper "0.1.0"
  :description "HTML Scraper"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [hickory "0.7.1"]
                 [com.github.vitobasso/hickory-css-selector "custom-for-papershop-13765b8140-1"]
                 [io.forward/yaml "1.0.9"]
                 [clj-http "3.10.0"]]
  :repositories {"jitpack" {:url "https://jitpack.io"}}
  :test-selectors {:default (complement :remote)
                   :integration :remote
                   :all (constantly true)})
