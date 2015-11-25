(ns ^:figwheel-always barberry.core
    (:require [rum.core :as rum]
              [ajax.core :as ajax]))

(enable-console-print!)

(def app-state (atom {}))

(rum/defc application < rum/cursored-watch [app-state]
  (let [state @app-state
        orders (:orders state)
        pending (:pending orders)]
    [:ul {} (for [o pending]
              [:li {} (str o)])]))

(ajax/GET "http://localhost:8081/api/orders"
  {:handler #(swap! app-state assoc :orders %)
    :error-handler #(println %)})

(rum/mount
  (application app-state)
  (.getElementById js/document "app"))
