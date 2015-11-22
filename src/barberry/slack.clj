(ns barberry.slack
  (:gen-class)
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [clojure.data.json :as json]
            [clojure.core.async :refer [go go-loop]]
            [barberry.config :refer [config]]))

(def RTM-START-URL "https://slack.com/api/rtm.start")

(defonce rtm-connection (atom nil))

(defn bot-handshake [{ok :ok url :url :as resp}]
  (if-not ok
    (:error resp)
    (let [conn @(http/websocket-client url)]
      (println "rtm connected to" url)
      (when @rtm-connection
        (s/close! @rtm-connection))
      (reset! rtm-connection conn)
      (go-loop []
        (when-let [msg @(s/take! conn)]
          (-> msg
              (json/read-str :key-fn keyword)
              println)
          (recur)))
      "bot is connected!")))

(defn run-bot [& a]
  (-> @(http/get RTM-START-URL
            {:query-params {:token (config :slack :token)
                            :no_unreads true}})
      :body
      bs/to-string
      (json/read-str :key-fn keyword)
      bot-handshake))

(defn bot-status [])
