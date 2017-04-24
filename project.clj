(defproject kite-notifier "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.2.0"]
                 [uswitch/lambada "0.1.2"]
                 [org.clojure/data.zip "0.1.1"]
                 [amazonica "0.3.48" :exclusions [com.amazonaws/aws-java-sdk com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.10.49"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.49"]
                 [clj-time "0.13.0"]
                 [twitter-api "1.8.0"]]
  :aot :all
  :main kite-notifier.core)
