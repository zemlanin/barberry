(ns barberry.slack
  (:gen-class)
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.stream :as s]
            [clojure.data.json :as json]
            [clojure.core.async :refer [go go-loop <!! timeout]]
            [clojure.core.match :refer [match]]
            [barberry.config :refer [config]]))

(def RTM-START-URL "https://slack.com/api/rtm.start")

(defonce rtm-connection (atom nil))
(defonce rtm-msg-id (atom 0))

(defn get-id []
  (swap! rtm-msg-id inc))

(defn is-dm-channel? [channel]
  (.startsWith (name channel) "D"))

(defn is-own-msg? [conn]
  #(= % (-> conn :self :id)))

(defn send-msg [conn msg]
  (go (s/put! (:stream conn) (json/write-str (merge msg {:id (get-id)})))))

(defn route-msg [conn msg]
  (match msg
    {:user (u :guard (is-own-msg? conn))} (println "self" msg)
    {:type "message"
      :channel (c :guard is-dm-channel?)
      :text text} (send-msg conn {:type :message
                                  :text text
                                  :channel c})
    :else (println msg)))

(defn bot-handshake [{ok :ok url :url :as resp}]
  (if-not ok
    (:error resp)
    (let [conn (merge resp {:stream @(http/websocket-client url)})]
      (println "rtm connected to" url)
      (when @rtm-connection
        (s/close! (:stream @rtm-connection)))
      (reset! rtm-connection conn)
      (go-loop []
        (when-not (s/closed? (:stream conn)) (send-msg conn {:type :ping}))
        (<!! (timeout 15000))
        (recur))
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
