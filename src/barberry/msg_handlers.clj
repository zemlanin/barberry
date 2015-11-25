(ns barberry.msg-handlers
  (:gen-class)
  (:require [barberry.config :refer [config]]
            [barberry.rtm :refer [send-msg connection]]
            [clojure.string :as string]
            [barberry.redis :as redis]))

(defn get-mention-str [user-id]
  (str "<@" user-id ">"))

(defn get-self-mention-str []
  (-> @connection :self :id get-mention-str))

(defn is-dm-channel? [{channel :channel}]
  (when channel
    (.startsWith (name channel) "D")))

(defn- is-mention? [{channel :channel text :text :as msg}]
  (or
    (is-dm-channel? msg)
    (when text
      (.startsWith text (get-self-mention-str)))))

(defn strip-mention [text]
  (string/replace
    text
    (re-pattern (str "^" (get-self-mention-str) "(\\s|:|,)+"))
    ""))

(defn- wanna-h [conn {channel :channel [whole item & _] :matches user :user :as msg}]
  (let [id (redis/wcar* (redis/incr "last-order-id"))
        order-map {:id id
                    :user user
                    :channel channel
                    :text item}]
    (redis/wcar*
      (redis/hset "orders" id order-map)
      (redis/sadd "pending" id))
    (send-msg conn {:type :message
                    :channel channel
                    :text (str
                            (when-not (is-dm-channel? msg)
                              (-> user get-mention-str (str ", ")))
                            "вот и сделай себе "
                            item)})))

(def wanna {:cond (fn [msg]
                    (and
                      (-> msg :type (= "message"))
                      (is-mention? msg)))
            :pattern #"^хочу (.*)"
            :handler wanna-h})
