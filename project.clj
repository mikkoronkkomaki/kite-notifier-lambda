(defproject kite-notifier "0.1.0-SNAPSHOT"
  :description "Kite notifications for Oulu Finland with an AWS Lambda"
  :url "https://github.com/mikkoronkkomaki/kite-notifier-lambda"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :source-paths ["src"]
  :test-paths ["test"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.2.0"]
                 [http-kit.fake "0.2.1"]
                 [uswitch/lambada "0.1.2"]
                 [org.clojure/data.zip "0.1.1"]
                 [amazonica "0.3.48" :exclusions [com.amazonaws/aws-java-sdk com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.10.49"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.49"]
                 [clj-time "0.13.0"]
                 [twitter-api "1.8.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [hiccup "1.0.5"]]

  :aot :all
  :main kite-notifier.core)
