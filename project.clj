(defproject barberry "0.1.0-SNAPSHOT"
  :description "barberry slack bot"
  :url "http://example.com/FIXME"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/txt/copying"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.1.19"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [jarohen/nomad "0.7.2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [camel-snake-kebab "0.3.2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [hiccup "1.0.5"]
                 [prismatic/schema "1.0.1"]
                 [com.taoensso/carmine "2.12.0"]
                 [aleph "0.4.1-beta2"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src-cljs"]
                        :figwheel true
                        :compiler { :main "barberry.core"
                                    :output-to "resources/public/js/compiled/barberry.js"
                                    :output-dir "resources/public/js/compiled"
                                    :asset-path "/static/js/compiled"}}
                       {:id "min"
                        :source-paths ["src-cljs"]
                        :compiler { :main "barberry.core"
                                    :output-to "resources/public/js/compiled/barberry.js"
                                    :asset-path "/static/js/compiled"
                                    :optimizations :advanced
                                    :pretty-print false}}]}

  :figwheel {:server-ip "0.0.0.0"
             :css-dirs ["resources/public/css"]
             :ring-handler barberry.core/my-app-reload}

  :main ^:skip-aot barberry.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
