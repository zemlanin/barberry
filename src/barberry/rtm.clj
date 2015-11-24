(ns barberry.rtm
  (:gen-class)
  (:require [manifold.stream :as s]
            [clojure.data.json :as json]
            [clojure.core.async :refer [go]]))

(defonce connection (atom nil))
(defonce msg-id (atom 0))

(defn get-id []
  (swap! msg-id inc))

(defn send-msg [conn msg]
  (go
    (s/put!
      (:stream conn)
      (json/write-str
        (merge msg {:id (get-id)})))))
