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

(defn send-notification [token user title message]
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
                      :wind-speed (:value (latest-observation "windspeedms" observations))
                      :wind-gust (:value (latest-observation "windGust" observations))
                      :wind-direction (:value (latest-observation "winddirection" observations))
                      :temperature (:value (latest-observation "temperature" observations))}]
    weather-data))

(defn run-notifier []
  (let [fmi-api-key (or (System/getenv "fmiapikey") (slurp "../.kite-notifier/fmiapikey"))
        fmi-station (or (System/getenv "fmistation") (slurp "../.kite-notifier/fmistation"))
        pushovertoken (or (System/getenv "pushovertoken") (slurp "../.kite-notifier/pushovertoken"))
        pushoveruser (or (System/getenv "pushoveruser") (slurp "../.kite-notifier/pushoveruser"))
        weather-data (get-weather-data fmi-api-key fmi-station)
        {:keys [time wind-speed wind-gust wind-direction temperature]} weather-data
        message (format "Mitattu: %s, tuulen nopeus %s m/s, puuskat: %s m/s, suunta: %s, lämpötila: %s"
                        time wind-speed wind-gust wind-direction temperature)]
    (send-notification pushovertoken
                       pushoveruser
                       "Tuuli olosuhteet Vihreäsaaren asemasta"
                       message)))

(deflambdafn kite-notifier.core.lambda [in out ctx]
  (println "Start " in ", " out ", " ctx)
  (run-notifier)
  (println "End"))
