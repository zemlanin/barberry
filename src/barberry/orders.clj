(ns barberry.orders)

(defn wrap-edn-response
  ([resp] (wrap-edn-response resp 200))
  ([resp status] {:body (pr-str resp)
                  :status status
                  :headers {"Content-Type" "application/edn; charset=utf-8"
                            "Access-Control-Allow-Origin" "*"}}))

(defn handler [r]
  (wrap-edn-response {:pending [{:id 1
                                  :item "tea"}]}))
