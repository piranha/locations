(ns locations.api
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [locations.utils :refer [promise->ch]]))


(defn map-options []
  (let [defaults {:center [55.751574, 37.573856],
                  :zoom 9}]
    (go (try
          (let [res (<! (-> (js/ymaps.geolocation.get #js {:autoReverseGeocode true})
                            promise->ch))
                geo (-> res .-geoObjects (.get 0))]
            (if geo
              {:bounds (-> geo .-properties (.get "boundedBy"))}
              defaults))
          (catch js/Error e
            (println "position detection fail" e)
            defaults)))))

(defn create-map [node options]
  (js/ymaps.Map. node options))

(defn get-map-node [map-obj]
  (-> map-obj .-container .getElement))

(let [cache (atom {})]
  (defn get-location [text bounds]
    (go (when-not (@cache [text bounds])
          (->
           (<! (promise->ch
                (js/ymaps.geocode text
                                  #js {:boundedBy bounds
                                       :results 1})))
           .-geoObjects
           (.get 0)
           ;; could be 'undefined' here if no results
           (#(swap! cache assoc [text bounds] %))))
        (@cache [text bounds]))))

(defn add-point [map-o point]
  (-> map-o .-geoObjects (.add point)))

(defn remove-point [map-o point]
  (-> map-o .-geoObjects (.remove point)))
