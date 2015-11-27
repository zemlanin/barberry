(ns barberry.orders
  (:require [barberry.redis :as redis]))

(defn wrap-edn-response
  ([resp] (wrap-edn-response resp 200))
  ([resp status] {:body (pr-str resp)
                  :status status
                  :headers {"Content-Type" "application/edn; charset=utf-8"
                            "Access-Control-Allow-Origin" "*"}}))

(defn handler [r]
  (let [pending-ids (redis/wcar* (redis/smembers "pending"))
        pending-orders (if (empty? pending-ids)
                        []
                        (redis/wcar* (apply redis/hmget "orders" pending-ids)))]
    (wrap-edn-response {:pending pending-orders})))
