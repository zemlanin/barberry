(ns barberry.core
  (:gen-class)
  (:require [ring.middleware.reload :as reload]
            [ring.middleware.keyword-params]
            [ring.middleware.params]
            [ring.middleware.cookies]
            [ring.middleware.session]
            [ring.middleware.anti-forgery]
            [ring.middleware.edn]
            [compojure.core :refer :all]
            [compojure.route]
            [org.httpkit.server :refer [run-server]]
            [clojure.data.json :as json]
            [camel-snake-kebab.core :refer [->camelCaseString]]
            [barberry.config :refer [config]]
            [barberry.slack :as slack]
            [barberry.orders :as orders]))

(defn wrap-json-response [resp]
  (-> resp
      (json/write-str :key-fn ->camelCaseString)
      (#(hash-map :body %
                  :headers {"Content-Type" "application/json; charset=utf-8"
                            "Access-Control-Allow-Origin" "*"}))))

(def web-routes
  (wrap-routes
    (routes
      (GET "/" [] (wrap-json-response "Hello World")))

    #(-> %
          ring.middleware.anti-forgery/wrap-anti-forgery
          ring.middleware.session/wrap-session)))

(defroutes api-routes
  (context "/api" []
    (GET "/orders" [] orders/handler)
    (PUT "/take-order" [] orders/take-order)
    (PUT "/finish-order" [] orders/finish-order)
    (PUT "/delete-order" [] orders/delete-order)))

(defroutes all-routes
  (if (config :debug) (compojure.route/resources "/static/") {})
  web-routes
  api-routes)

(def my-app
  (-> all-routes
      ring.middleware.edn/wrap-edn-params
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      ring.middleware.cookies/wrap-cookies))

(def my-app-reload
  (-> my-app
      (reload/wrap-reload {:dirs ["src"]})))

(def run-bot-reload
  (-> slack/run-bot
      (reload/wrap-reload {:dirs ["src"]})))

(defn -main [& args]
  (let [handler (if (config :debug) my-app-reload my-app)
        run-bot (if (config :debug) run-bot-reload slack/run-bot)]
    (run-bot 1) ; for reload/wrap-reload
    (run-server handler {:port (config :port)})))
