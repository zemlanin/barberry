(ns barberry.redis
  (:gen-class)
  (:refer-clojure :exclude [get set key])
  (:require [taoensso.carmine :as car]
            [barberry.config :refer [config]]))

(def server1-conn {:pool nil :spec (config :redis)})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def ping car/ping)
(def incr car/incr)
(def key car/key)
(def get car/get)
(def set car/set)
(def setex car/setex)
(def del car/del)
(def hget car/hget)
(def hmget car/hmget)
(def hset car/hset)
(def hsetnx car/hsetnx)
(def sadd car/sadd)
(def smembers car/smembers)
