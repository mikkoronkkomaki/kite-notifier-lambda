(ns kite-notifier.core
  (:import (java.io ByteArrayInputStream)
           (org.xml.sax SAXParseException))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [clojure.zip :refer [xml-zip]]
            [clojure.data.zip.xml :as z]
            [clojure.xml :refer [parse]]
            [uswitch.lambada.core :refer [deflambdafn]]
            [clj-time.local :as l]
            [clj-time.core :as t])
  (:use [amazonica.aws.s3])
  (:gen-class))

(defn send-notification [token user {:keys [title message]}]
  (let [options {:form-params {:token token
                               :user user
                               :message message
                               :title title
                               :url "https://pushover.net/api"}}
        {:keys [status error body]} @(http/post "https://api.pushover.net/1/messages.json" options)]
    (println "Response: " status ", " error ", " body)))

(defn get-weather-data [api-key station-id]
  (let [read-xml (fn [xml]
                   (let [in (ByteArrayInputStream. (.getBytes xml "UTF-8"))]
                     (try (xml-zip (parse in))
                          (catch SAXParseException e
                            (println "An exception occured while parsing the XML payload:" xml)
                            nil))))
        xml (:body @(http/get (str "http://data.fmi.fi/fmi-apikey/"
                                   api-key
                                   "/wfs?request=getFeature&storedquery_id=fmi::observations::weather::simple&fmisid="
                                   station-id
                                   "&parameters=windspeedms,winddirection,windGust,temperature")))
        handle-observation (fn [observation]
                             {:time (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:Time z/text)
                              :parameter (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:ParameterName z/text)
                              :value (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:ParameterValue z/text)})
        latest-observation (fn [parameter observations]
                             (first (filter #(= (:parameter %) parameter) observations)))
        observations (reverse (sort-by :time (z/xml-> (read-xml xml) :wfs:member handle-observation)))
        weather-data {:time (:time (latest-observation "windspeedms" observations))
                      :wind-speed (Double/parseDouble (:value (latest-observation "windspeedms" observations)))
                      :wind-gust (Double/parseDouble (:value (latest-observation "windGust" observations)))
                      :wind-direction (Double/parseDouble (:value (latest-observation "winddirection" observations)))
                      :temperature (Double/parseDouble (:value (latest-observation "temperature" observations)))}]
    (println "Current weather data: " weather-data)
    weather-data))

(defn between? [min max val]
  (and (<= min val) (>= max val)))

(defn conditions-good? [wind-speed wind-gust wind-direction]
  (and (between? 7 15 wind-speed)
       (between? 7 16 wind-gust)
       (> 4 (- wind-gust wind-speed))
       (between? 180 360 wind-direction)))

(defn strong-wind? [wind-speed]
  (< 15 wind-speed))

(defn strong-gusts? [wind-speed wind-gust]
  (<= 4 (- wind-gust wind-speed)))

(defn wind-direction-explanation [wind-direction]
  (cond
    (between? 22.5M 67.5M wind-direction) "Koilinen"
    (between? 67.5M 112.5M wind-direction) "Itä"
    (between? 112.5M 157.5M wind-direction) "Kaakko"
    (between? 157.5M 202.5M wind-direction) "Etelä"
    (between? 202.5M 247.5M wind-direction) "Lounas"
    (between? 247.5M 292.5M wind-direction) "Länsi"
    (between? 292.5M 337.5M wind-direction) "Luode"
    (between? 337.5M 22.5M wind-direction) "Luode"
    :else "Suunta ei saatavilla"))

(defn read-config [environment-key path]
  (or (System/getenv environment-key) (slurp path)))

(defn notification [{:keys [time wind-speed wind-gust wind-direction temperature] :as weather-data} station]
  (let [title (cond (strong-wind? wind-speed) (format "Varoitus! Kova tuuli mitattu asemalla: %s." station)
                    (strong-gusts? wind-speed wind-gust) (format "Varoitus! Kovia puuskia mitattu asemalla: %s." station)
                    (conditions-good? wind-speed wind-gust wind-direction) (format "Loistavat olosuhteet mitattu asemalla: %s." station))
        wind-direction (wind-direction-explanation wind-direction)
        message (format "Mitattu: %s, tuulen nopeus %s m/s, puuskat: %s m/s, suunta: %s, lämpötila: %s"
                        time wind-speed wind-gust wind-direction temperature)]
    {:title title
     :message message}))

(defn write-setting-to-s3 [bucket key]
  (let [data ""
        bytes (.getBytes data)
        input-stream (ByteArrayInputStream. bytes)]
    (put-object :bucket-name bucket
                :key key
                :input-stream input-stream
                :return-values "ALL_NEW"
                :metadata {:content-length (count bytes)})))

(defn read-setting-from-s3 [bucket key]
  (:last-modified (:object-metadata (get-object bucket "settings.clj"))))

(defn write-settings [bucket notification-sent? warning-sent?]
  (when notification-sent? (write-setting-to-s3 bucket "last-notification"))
  (when warning-sent? (write-setting-to-s3 bucket "last-warning")))

(defn read-settings [bucket]
  {:last-notification (read-setting-from-s3 bucket "last-notification")
   :last-warning (read-setting-from-s3 bucket "last-warning")})

(defn minutes-ago [time]
  (t/in-minutes (t/interval time (t/now))))

(defn run-notifier []
  (let [bucket (read-config "bucket" "../.kite-notifier/bucket")
        fmi-api-key (read-config "fmiapikey" "../.kite-notifier/fmiapikey")
        fmi-station (read-config "fmistation" "../.kite-notifier/fmistation")
        pushovertoken (read-config "pushovertoken" "../.kite-notifier/pushovertoken")
        pushoveruser (read-config "pushoveruser" "../.kite-notifier/pushoveruser")
        {:keys [wind-speed wind-gust wind-direction] :as weather-data} (get-weather-data fmi-api-key fmi-station)
        {:keys [last-notification last-warning]} (read-settings bucket)
        send-notification? (> 360 (minutes-ago last-notification))
        send-warning? (> 360 (minutes-ago last-warning))]
    (when (or (and (strong-wind? wind-speed) send-warning?)
              (and (strong-gusts? wind-speed wind-gust) send-warning?)
              (and (conditions-good? wind-speed wind-gust wind-direction)))
      (let [notification (notification weather-data "Vihreäsaari")]
        (println "Notification: " notification)
        (send-notification pushovertoken pushoveruser notification)
        (write-settings bucket
                        (or (strong-wind? wind-speed) (strong-gusts? wind-speed wind-gust))
                        (conditions-good? wind-speed wind-gust wind-direction))))
    weather-data))

(deflambdafn kite-notifier.core.lambda [in out ctx]
  (println "Start " in ", " out ", " ctx)
  (let [weather-data (run-notifier)]
    (println "End")
    weather-data))
