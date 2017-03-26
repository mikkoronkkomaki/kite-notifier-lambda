(ns kite-notifier.core
  (:import (java.io ByteArrayInputStream)
           (org.xml.sax SAXParseException))
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer [parse-string]]
            [org.httpkit.client :as http]
            [clojure.zip :refer [xml-zip]]
            [clojure.data.zip.xml :as z]
            [clojure.xml :refer [parse]]
            [uswitch.lambada.core :refer [deflambdafn]])
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

(defn notification [{:keys [time wind-speed wind-gust wind-direction temperature] :as weather-data} station]
  (let [title (cond (strong-wind? wind-speed) (format "Varoitus! Kova tuuli mitattu asemalla: %s." station)
                    (strong-gusts? wind-speed wind-gust) (format "Varoitus! Kovia puuskia mitattu asemalla: %s." station)
                    (conditions-good? wind-speed wind-gust wind-direction) (format "Loistavat olosuhteet mitattu asemalla: %s." station))
        wind-direction (wind-direction-explanation wind-direction)
        message (format "Mitattu: %s, tuulen nopeus %s m/s, puuskat: %s m/s, suunta: %s, lämpötila: %s"
                        time wind-speed wind-gust wind-direction temperature)]
    (when title {:title title
                 :message message})))

(defn run-notifier []
  (let [fmi-api-key (or (System/getenv "fmiapikey") (slurp "../.kite-notifier/fmiapikey"))
        fmi-station (or (System/getenv "fmistation") (slurp "../.kite-notifier/fmistation"))
        pushovertoken (or (System/getenv "pushovertoken") (slurp "../.kite-notifier/pushovertoken"))
        pushoveruser (or (System/getenv "pushoveruser") (slurp "../.kite-notifier/pushoveruser"))
        weather-data (get-weather-data fmi-api-key fmi-station)
        notification (notification weather-data "Vihreäsaari")]
    (when notification
      (println "Notification: " notification)
      (send-notification pushovertoken pushoveruser notification))))

(deflambdafn kite-notifier.core.lambda [in out ctx]
  (println "Start " in ", " out ", " ctx)
  (run-notifier)
  (println "End"))
