(ns barberry.slack
  (:gen-class)
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [clojure.data.json :as json]
            [barberry.config :refer [config]]))

(def RTM-START-URL "https://slack.com/api/rtm.start")

(defn bot-handshake [{ok :ok url :url :as resp}]
  (if-not ok
    (:error resp)
    (let [conn @(http/websocket-client url)]
      (println "rtm connected to" url)
      (-> @(s/take! conn)))))

(defn run-bot [& a]
  (-> @(http/get RTM-START-URL
            {:query-params {:token (config :slack :token)
                            :no_unreads true}})
      :body
      bs/to-string
      (json/read-str :key-fn keyword)
      bot-handshake))

(defn bot-status [])