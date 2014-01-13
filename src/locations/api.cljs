(ns locations.api)

(defn init-map [app-state]
  (swap! app-state assoc-in [:map :constructor] ymaps/Map)
  (let [store-options (fn [options]
                        (swap! app-state assoc-in [:map :options] (clj->js options)))
        defaults {:center [55.751574, 37.573856],
                  :zoom 9}
        succ (fn [res]
               (let [geo (-> res .-geoObjects (.get 0))
                     options (if geo
                               {:bounds (-> geo .-properties (.get "boundedBy"))}
                               defaults)]
                 (store-options options)))
        fail (fn [err]
               (println err)
               (store-options defaults))
        promise (ymaps.geolocation/get #js {:autoReverseGeocode true})]
    (.then promise succ fail)))
