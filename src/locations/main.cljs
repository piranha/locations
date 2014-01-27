(ns locations.main
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [cljs.core.async :refer [chan put! >! <!]]
            [locations.api :refer [map-options get-location]]
            [locations.views :as views]
            [locations.utils :refer [clean-address parse-locations control-chan]]))

(enable-console-print!)

(extend-type string
  ICloneable
  (-clone [x] (js/String. x)))

(def app-state (atom {:state :edit
                      :input "Лукьяновская, 7"
                      :locations []
                      :points {}
                      :map {:options nil
                            :constructor nil
                            :object nil}}))

#_ (add-watch app-state ::qwe
           (fn [_ _ _ new]
             (println (:locations new))))

(defn search [data]
  (let [{:keys [locations points map]} @data
        {:keys [bounds object]} map]
    (om/transact! data :points select-keys locations)
    (doseq [{:keys [text]} locations]
      (go
       (let [point (<! (get-location text bounds))]
         (om/update! data assoc-in [:points text] point))))))

(defn handle-event [ev owner]
  (let [data (om/get-props owner)]
    (condp = ev
      :search (do (om/update! data merge
                              {:state :display
                               :points {}
                               :locations (parse-locations (:input @data))})
                  (search data))
      :edit (do (om/update! data assoc-in [:state] :edit))
      js/console.log)))

(defn root [{:keys [state input locations] :as data} owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (om/set-state! owner :control
                     (control-chan handle-event owner)))

    om/IRenderState
    (render-state [this {:keys [control]}]
      (html [:div.container
             [:div.row
              (condp = state
                :edit (om/build views/address-input
                                input {:opts control})
                :display (om/build views/address-display
                                   locations {:opts control})
                [:div (str "Unknown :state - " state)])
              (om/build views/map-container data)]]))))


(om/root app-state root js/document.body)
(js/ymaps.ready
 (fn []
   (go (let [options (<! (map-options))]
         (swap! app-state assoc-in [:map :options]
                (clj->js options))))))
