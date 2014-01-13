(ns locations.core
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [locations.api :refer [init-map]]
            [locations.views :refer [control map-container]]
            [locations.utils :refer [clean-address]]))

(enable-console-print!)

(def app-state (atom {:map {:options nil
                            :constructor nil
                            :object nil}}))

(defn root [data]
  (om/component
   (html [:div.container
          [:div.row
           (om/build control data)
           (om/build map-container data)]])))

(om/root app-state root js/document.body)

(ymaps/ready #(init-map app-state))
