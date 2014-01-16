(ns locations.utils
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [clojure.string :as s]
            [cljs.core.async :refer [chan put! >! <!]]))


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
  (println text)
  (->> (s/split text #"\n")
       (mapv s/trim)
       (filterv seq)
       (mapv #(identity {:text % :clear (clean %)}))))

(defn control-chan [handler]
  (let [c (chan)]
    (go (loop [e (<! c)]
          (when e
            (handler e)
            (recur (<! c)))))
    c))