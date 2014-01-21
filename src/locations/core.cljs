(ns locations.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [cljs.core.async :refer [chan put! >! <!]]
            [locations.api :refer [init-map]]
            [locations.views :as views]
            [locations.utils :refer [clean-address parse-locations control-chan]]))

(enable-console-print!)

(extend-type string
  ICloneable
  (-clone [x] (js/String. x)))

(def app-state (atom {:state :edit
                      :input "qwe"
                      :locations []
                      :map {:options nil
                            :constructor nil
                            :object nil}}))

#_ (add-watch app-state ::qwe
           (fn [_ _ _ new]
             (println (:locations new))))

(defn root [{:keys [state input locations] :as data} owner]
  (let [search (fn []
                 (println "Searching..."))
        handle-event (fn [ev]
                       (let [data (om/get-props owner)]
                         (condp = ev
                           :search (do (om/update! data assoc-in [:state] :display)
                                       (om/update! data update-in [:locations]
                                                   #(parse-locations (:input data)))
                                       (search))
                           :edit (do (println "EDIT")
                                     (om/update! data assoc-in [:state] :edit))
                           println)))]

    (reify
      om/IWillMount
      (will-mount [this]
        (om/set-state! owner :control
                       (control-chan handle-event)))

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
                (om/build views/map-container data)]])))))


(om/root app-state root js/document.body)
(js/ymaps.ready #(init-map app-state))
