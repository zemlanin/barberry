(ns ^:figwheel-always barberry.core
    (:require-macros [cljs.core.async.macros :refer [go-loop]])
    (:require [rum.core :as rum]
              [ajax.core :as ajax]
              [cljs.core.async :refer [timeout <!]]
              [ajax.edn :refer [edn-request-format edn-response-format]]
              [datascript.core :as d]))

(enable-console-print!)

(def app-state (atom {}))
(def conn (d/create-conn {}))

(defn refresh-orders []
  (ajax/GET "/api/orders"
    {:handler (fn [resp]
                (d/transact! conn (->> resp :pending (map (fn [x] {:order/id (:id x)
                                                                   :order/text (:text x)
                                                                   :order/user (:user x)}))))
                (swap! app-state assoc :orders resp))
      :format (edn-request-format)
      :response-format (edn-response-format)
      :error-handler println}))

(defn take-order [order]
  (ajax/PUT "/api/take-order"
    {:params order
      :format (edn-request-format)
      :handler #(refresh-orders)
      :error-handler println}))

(defn finish-order [order]
  (ajax/PUT "/api/finish-order"
    {:params order
      :format (edn-request-format)
      :handler #(refresh-orders)
      :error-handler println}))

(defn delete-order [order]
  (ajax/PUT "/api/delete-order"
    {:params order
      :format (edn-request-format)
      :handler #(refresh-orders)
      :error-handler println}))

(rum/defc application < rum/cursored-watch [app-state]
  (let [state @app-state
        orders (:orders state)
        pending (:pending orders)
        taken (:taken orders)
        finished (:finished orders)]
    [:div
      [:h3 {} "pending"]
      [:ul {} (for [o pending]
                [:li {}
                  [:span (str o)]
                  [:button {:onClick #(take-order o)} "take"]])]
      [:h3 {} "taken"]
      [:ul {} (for [o taken]
                [:li {}
                  [:span (str o)]
                  [:button {:onClick #(finish-order o)} "finish"]])]
      [:h3 {} "finished"]
      [:ul {} (for [o finished]
                [:li {}
                  [:span (str o)]
                  [:button {:onClick #(delete-order o)} "delete"]])]]))

(refresh-orders)
; (go-loop []
;   (refresh-orders)
;   (<! (timeout 10000))
;   (recur))

(rum/mount
  (application app-state)
  (.getElementById js/document "app"))
