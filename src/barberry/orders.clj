(ns barberry.orders
  (:require [barberry.redis :as redis]
            [barberry.rtm :refer [send-msg connection]]))

(defn wrap-edn-response
  ([resp] (wrap-edn-response resp 200))
  ([resp status] {:body (pr-str resp)
                  :status status
                  :headers {"Content-Type" "application/edn; charset=utf-8"
                            "Access-Control-Allow-Origin" "*"}}))

(defn handler [r]
  (let [[pending-ids taken-ids finished-ids] (redis/wcar*
                                              (redis/smembers "pending")
                                              (redis/smembers "taken")
                                              (redis/smembers "finished"))
        pending-orders (if (empty? pending-ids)
                        []
                        (redis/wcar* (apply redis/hmget "orders" pending-ids)))
        taken-orders (if (empty? taken-ids)
                        []
                        (redis/wcar* (apply redis/hmget "orders" taken-ids)))
        finished-orders (if (empty? finished-ids)
                          []
                          (redis/wcar* (apply redis/hmget "orders" finished-ids)))]
    (wrap-edn-response {:pending pending-orders
                        :taken taken-orders
                        :finished finished-orders})))

(defn take-order [{order :edn-params :as r}]
  (redis/wcar* (redis/smove "pending" "taken" (:id order)))
  (send-msg @connection {:type :message
                          :channel (:channel order)
                          :text (str "Заказ на " (:text order) " принят")})
  {})

(defn finish-order [{order :edn-params :as r}]
  (redis/wcar* (redis/smove "taken" "finished" (:id order)))
  (send-msg @connection {:type :message
                          :channel (:channel order)
                          :text (str "Заказ на " (:text order) " готов")})
  {})

(defn delete-order [{order :edn-params :as r}]
  (redis/wcar*
    (redis/srem "finished" (:id order))
    (redis/hdel "orders" (:id order)))
  {})
