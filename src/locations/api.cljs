(ns locations.api
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [locations.utils :refer [<?] :include-macros true]))


(defn init-map [app-state]
  (swap! app-state assoc-in [:map :constructor] js/ymaps.Map)

  (let [defaults {:center [55.751574, 37.573856],
                  :zoom 9}
        store-options (fn [options]
                        (swap! app-state assoc-in [:map :options] (clj->js options)))]

    (go (try
          (let [res (<? (js/ymaps.geolocation.get #js {:autoReverseGeocode true}))
                geo (-> res .-geoObjects (.get 0))
                options (if geo
                          {:bounds (-> geo .-properties (.get "boundedBy"))}
                          defaults)]
            (store-options options))
          (catch js/Error e
            (println e)
            (store-options defaults))))))
