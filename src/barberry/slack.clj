(ns barberry.slack
  (:gen-class)
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [clojure.data.json :as json]
            [clojure.core.async :refer [go go-loop <!! timeout]]
            [clojure.core.match :refer [match]]
            [barberry.msg-handlers]
            [barberry.rtm :refer [connection send-msg]]
            [barberry.config :refer [config]]))

(def RTM-START-URL "https://slack.com/api/rtm.start")

(defn is-own-msg? [{user :user}]
  (= user (-> @connection :self :id)))

(defn route-msg [conn msg]
  (if (is-own-msg? msg)
    (print "self ")
    (do
      (doseq [{p :pattern h :handler} (->> 'barberry.msg-handlers ns-publics vals (map var-get))
              :when (and p h (p msg))]
        (h conn msg))))
  (println msg))

(defn bot-handshake [{ok :ok url :url :as resp}]
  (if-not ok
    (:error resp)
    (let [conn (merge resp {:stream @(http/websocket-client url)})]
      (println "rtm connected to" url)
      (when @connection
        (s/close! (:stream @connection)))
      (reset! connection conn)
      (go-loop []
        (when-not (s/closed? (:stream conn))
          (send-msg conn {:type :ping})
          (<!! (timeout 15000))
          (recur)))
      (go-loop []
        (when-let [msg @(s/take! (:stream conn))]
          (route-msg conn (json/read-str msg :key-fn keyword))
          (recur)))
      "bot is connected!")))

(defn run-bot [& a]
  (-> @(http/get RTM-START-URL
            {:query-params {:token (config :slack :token)
                            :no_unreads true
                            :simple_latest true}})
      :body
      bs/to-string
      (json/read-str :key-fn keyword)
      bot-handshake))

(defn bot-status [])
