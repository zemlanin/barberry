(ns barberry.msg-handlers
  (:gen-class)
  (:require [barberry.config :refer [config]]
            [barberry.rtm :refer [send-msg connection]]))

(defn- is-dm-channel? [{channel :channel text :text}]
  (or
    (when channel
      (.startsWith (name channel) "D"))
    (when text
      (.startsWith text (str "<@" (-> @connection :self :id) ">")))))

(defn- echo [conn msg]
  (send-msg conn {:type :message
                  :channel (:channel msg)
                  :text (:text msg)}))

(def e {:pattern (fn [msg]
                    (and
                      (-> msg :type (= "message"))
                      (is-dm-channel? msg)))
        :handler echo})
