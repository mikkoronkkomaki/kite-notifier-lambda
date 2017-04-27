(ns kite-notifier.core
  (:import (java.io ByteArrayInputStream)
           (org.xml.sax SAXParseException))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [uswitch.lambada.core :refer [deflambdafn]]
            [clj-time.local :as l]
            [clj-time.core :as t]
            [kite-notifier.twitter :as twitter]
            [kite-notifier.pushover :as pushover]
            [kite-notifier.fmi :as fmi]
            [kite-notifier.s3 :as s3]
            [kite-notifier.weather-data :refer [conditions-good? strong-wind? strong-gusts?]])
  (:gen-class))

(defn read-config [environment-key path]
  (or (System/getenv environment-key) (slurp path)))

(defn write-settings [bucket notification-sent? warning-sent?]
  (when notification-sent?
    (s3/write-setting-to-s3 bucket "last-notification"))
  (when warning-sent?
    (s3/write-setting-to-s3 bucket "last-warning")))

(defn read-settings [bucket]
  {:last-notification (s3/read-setting-from-s3 bucket "last-notification")
   :last-warning (s3/read-setting-from-s3 bucket "last-warning")})

(defn hours-ago [time]
  (t/in-hours (t/interval time (t/now))))

(defn run-notifier []
  (let [bucket (read-config "bucket" "../.kite-notifier/bucket")
        fmi-api-key (read-config "fmiapikey" "../.kite-notifier/fmiapikey")
        fmi-station (read-config "fmistation" "../.kite-notifier/fmistation")
        pushovertoken (read-config "pushovertoken" "../.kite-notifier/pushovertoken")
        pushoveruser (read-config "pushoveruser" "../.kite-notifier/pushoveruser")
        twitter-app-consumer-key (read-config "twitterappconsumerkey" "../.kite-notifier/twitter-app-consumer-key")
        twitter-app-consumer-secret (read-config "twitterappconsumersecret" "../.kite-notifier/twitter-app-consumer-secret")
        twitter-user-access-token (read-config "twitteruseraccesstoken" "../.kite-notifier/twitter-user-access-token")
        twitter-user-access-token-secret (read-config "twitteruseraccesstokensecret" "../.kite-notifier/twitter-user-access-token-secret")
        {:keys [wind-speed wind-gust wind-direction] :as weather-data} (fmi/get-weather-data fmi-api-key fmi-station)
        {:keys [last-notification last-warning]} (read-settings bucket)
        send-notification? (< 6 (hours-ago last-notification))
        send-warning? (< 6 (hours-ago last-warning))]

    (println (str "Last notification sent " (< 6 (hours-ago last-notification) " hours ago")))
    (println (str "Last warning sent " (< 6 (hours-ago last-warning) " hours ago")))

    (if (or (and send-notification? (conditions-good? wind-speed wind-gust wind-direction))
            (and send-warning? (strong-wind? wind-speed))
            (and send-warning? (strong-gusts? wind-speed wind-gust)))
      (do (println "Sending notification:" (and (conditions-good? wind-speed wind-gust wind-direction) send-notification?)
                   " & warning:" (or (strong-wind? wind-speed) (strong-gusts? wind-speed wind-gust)))
          (pushover/send-notification pushovertoken pushoveruser weather-data "Vihreäsaari")
          (twitter/post twitter-app-consumer-key
                        twitter-app-consumer-secret
                        twitter-user-access-token
                        twitter-user-access-token-secret
                        weather-data
                        "Vihreäsaari")
          (write-settings bucket
                          (conditions-good? wind-speed wind-gust wind-direction)
                          (or (strong-wind? wind-speed) (strong-gusts? wind-speed wind-gust))))
      (println "No notification/warning sent"))
    weather-data))

(deflambdafn kite-notifier.core.lambda [in out ctx]
  (println "Start " in ", " out ", " ctx)
  (let [weather-data (run-notifier)]
    (println "End")
    weather-data))





