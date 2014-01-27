(defproject locations "0.1.0-SNAPSHOT"
  :description "Locations"
  :url "http://locations.in.ua"

  :dependencies [[org.clojure/clojurescript "0.0-2138"]
                 [om "0.3.0"]
                 [sablono "0.2.3"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

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
