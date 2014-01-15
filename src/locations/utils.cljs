(ns locations.utils
  (:require [clojure.string :as s]))


(defn clean [value]
  (-> value
      (s/replace "\u2019" "'")
      (s/replace "&nbsp;" " ")
      ;; &amp;, etc
      (s/replace #"&\w+;" "")
      ;; First/Second street (like a corner)
      (s/replace #"([^,0-9 ]+)/[^,0-9 ]+" "$1")
      ;; Address: some meaningful stuff
      (s/replace #"[^:]+:" "")
      ;; (some description)
      (s/replace #"\(.*?\)" "")))

(defn parse-locations [text]
  (->> (s/split text #"\n")
       (mapv s/trim)
       (filterv seq)))
