(ns locations.views
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [cljs.core.async :refer [chan put! >! <!]]
            [locations.utils :refer [clean-address]]))


(defn address-input [locations owner control-c]
  (let [update-locations (fn [e]
                           (om/update! locations
                                       #(identity (.. e -target -value ))))
        search (fn [e]
                 (.preventDefault e)
                 (put! control-c :search))]
    (om/component
     (html [:div.col-md-3
            [:header
             [:h1 "Locations"]]

            [:form
             [:div.row
              [:textarea {:style {:height "600px" :width "100%"}
                          :on-change update-locations
                          :value locations}]]
             [:div.row
              [:button.btn.btn-primary {:on-click search} "Search"]]]]))))


(defn address-display [locations owner control-c]
  (let [edit (fn [e]
               (.preventDefault e)
               (put! control-c :edit))]
    (om/component
     (html [:div.col-md-3
            [:header
             [:h1 "Locations"]

             [:ul
              (for [item locations]
                [:li item])]

             [:div.row
              [:button.btn.btn-primary {:on-click edit} "Edit"]]
             ]]))))

(defn map-container [data owner]
  (let [maybe-create-map (fn [node {:keys [constructor options object]}]
                           (when (and constructor options (not object))
                             (om/transact! data [:map :object] assoc
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
