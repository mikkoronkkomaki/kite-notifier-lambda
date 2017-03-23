(defproject kite-notifier "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.google.api-client/google-api-client "1.22.0"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.22.0"]
                 [com.google.apis/google-api-services-sheets "v4-rev462-1.22.0"]
                 [http-kit "2.2.0"]
                 [cheshire "5.7.0"]
                 [http-kit "2.2.0"]
                 [uswitch/lambada "0.1.2"]
                 [org.clojure/data.zip "0.1.1"]]
  :aot :all
  :main kite-notifier.core)
