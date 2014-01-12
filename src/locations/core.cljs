(ns locations.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

(enable-console-print!)

(def app-state (atom {:map {:options nil
                            :constructor nil
                            :object nil}}))

(defn control [data]
  (om/component
   (html [:div.col-md-3
          [:header
           [:h1 "Locations"]]

          [:form
           [:div.row
            [:textarea {:style {:height "600px" :width "100%"}}]]
           [:div.row
            [:button.btn.btn-primary "Search"]]]])))

(defn map-container [data owner]
  (let [maybe-create-map (fn [node {:keys [constructor options object]}]
                           (if (and constructor options (not object))
                             (om/update! data assoc [:map :object]
                                         (constructor. node options))))]
    (reify
      om/IDidMount
      (did-mount [this node]
        (maybe-create-map node (data :map)))

      om/IWillUpdate
      (will-update [this next-props next-state]
        (maybe-create-map (.getDOMNode owner) (next-props :map)))

      om/IRender
      (render [this]
        (html [:div#map-canvas.col-md-9
               {:style {:height "100%"}}])))))

(defn root [data]
  (om/component
   (html [:div.container
          [:div.row
           (om/build control data)
           (om/build map-container data)]])))

(om/root app-state root js/document.body)

(ymaps/ready
 (fn []
   (swap! app-state assoc-in [:map :constructor] ymaps/Map)
   (let [store-options (fn [options]
                         (swap! app-state assoc-in [:map :options] options))
         defaults #js {:center #js [55.751574, 37.573856],
                       :zoom 9}
         succ (fn [res]
                (let [geo (-> res .-geoObjects (.get 0))
                      options (if geo
                                #js {:bounds (-> geo .-properties (.get "boundedBy"))}
                                defaults)]
                  (store-options options)))
         fail (fn [err]
                (println err)
                (store-options defaults))
         promise (ymaps.geolocation/get #js {:autoReverseGeocode true})]
     (.then promise succ fail))))
