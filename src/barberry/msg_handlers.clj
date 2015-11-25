(ns barberry.msg-handlers
  (:gen-class)
  (:require [barberry.config :refer [config]]
            [barberry.rtm :refer [send-msg connection]]
            [clojure.string :as string]))


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

(defn- wanna-h [conn msg]
  (send-msg conn {:type :message
                  :channel (:channel msg)
                  :text (str
                          (when-not (is-dm-channel? msg)
                            (-> msg :user get-mention-str (str ", ")))
                          "вот и сделай себе "
                          (-> msg :matches second))}))

(def wanna {:cond (fn [msg]
                    (and
                      (-> msg :type (= "message"))
                      (is-mention? msg)))
            :pattern #"^хочу (.*)"
            :handler wanna-h})
