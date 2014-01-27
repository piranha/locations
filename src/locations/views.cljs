(ns locations.views
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [cljs.core.async :refer [chan put! >! <!]]
            [locations.api :refer [create-map get-map-node add-point remove-point]]
            [locations.utils :refer [clean-address]]))


(defn address-input [locations owner control-c]
  (let [update-locations (fn [e]
                           (om/update! locations
                                       #(identity (.. e -target -value))))
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


(defn single-address [{:keys [text clear] :as item}]
  (om/component
   (html
    [:li {:title clear
          :style {:background (if (om/join item [:points text])
                                "blue"
                                "red")}}
     text])))


(defn address-display [locations owner control-c]
  (let [edit (fn [e]
               (.preventDefault e)
               (put! control-c :edit))]
    (om/component
     (html [:div.col-md-3
            [:header
             [:h1 "Locations"]

             [:ul {:style {:height "600px" :overflow-y "scroll"}}
              (for [item locations]
                (om/build single-address item {:key :text}))]

             [:div.row
              [:button.btn.btn-primary {:on-click edit} "Edit"]]]]))))

(defn map-container [data owner]
  (let [maybe-create-map (fn [node {:keys [options object]}]
                           (when (and options (not object))
                             (om/update! data assoc-in [:map :object]
                                         (create-map node options))))]
    (reify
      om/IDidMount
      (did-mount [this node]
        (maybe-create-map node (data :map)))

      om/IWillUpdate
      (will-update [this next-props next-state]
        (maybe-create-map (.getDOMNode owner) (next-props :map))

        (let [points-old (:points (om/get-props owner))
              points-new (:points next-props)
              map-object (-> next-props :map :object)

              to-remove (for [[k point] points-old
                              :when (and point (not (points-new k)))]
                          point)
              to-add (for [[k point] points-new
                           :when (and point (not (points-old k)))]
                       point)]

          (when map-object
            (doseq [point to-remove]
              (remove-point map-object point))
            (doseq [point to-add]
              (add-point map-object point)))))

      om/IRender
      (render [this]
        (html [:div#map-canvas.col-md-9
               {:style {:height "100%"}}])))))
