(defproject locations "0.1.0-SNAPSHOT"
  :description "Locations"
  :url "http://locations.in.ua"

  :dependencies [[org.clojure/clojurescript "0.0-2138"]
                 [om "0.1.5"]
                 [sablono "0.2.0"]]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "locations"
              :source-paths ["src"]
              :compiler {
                :output-to "locations.js"
                :output-dir "_out"
                :optimizations :none
                :source-map true}}]})
