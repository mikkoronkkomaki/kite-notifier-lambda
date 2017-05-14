(ns kite-notifier.fmi
  (:require [clojure.data.zip.xml :as z]
            [clojure.zip :refer [xml-zip]]
            [clojure.xml :refer [parse]]
            [org.httpkit.client :as http]
            [kite-notifier.dates :as dates]
            [taoensso.timbre :as log])
  (:import (java.io ByteArrayInputStream)
           (org.xml.sax SAXParseException)))

(defn read-xml [xml]
  (let [in (ByteArrayInputStream. (.getBytes xml "UTF-8"))]
    (try (xml-zip (parse in))
         (catch SAXParseException e
           (log/error e "An exception occured while parsing the XML payload:" xml)
           nil))))

(defn handle-observation [observation]
  {:time (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:Time z/text)
   :parameter (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:ParameterName z/text)
   :value (z/xml1-> observation :BsWfs:BsWfsElement :BsWfs:ParameterValue z/text)})

(defn latest-observation [parameter observations]
  (first (filter #(= (:parameter %) parameter) observations)))

(defn get-weather-data [api-key station-id]
  (let [xml (:body @(http/get (str "http://data.fmi.fi/fmi-apikey/"
                                   api-key
                                   "/wfs?request=getFeature&storedquery_id=fmi::observations::weather::simple&fmisid="
                                   station-id
                                   "&parameters=windspeedms,winddirection,windGust,temperature")))
        observations (reverse (sort-by :time (z/xml-> (read-xml xml) :wfs:member handle-observation)))
        weather-data {:time (dates/parse (:time (latest-observation "windspeedms" observations)))
                      :wind-speed (Double/parseDouble (:value (latest-observation "windspeedms" observations)))
                      :wind-gust (Double/parseDouble (:value (latest-observation "windGust" observations)))
                      :wind-direction (Double/parseDouble (:value (latest-observation "winddirection" observations)))
                      :temperature (Double/parseDouble (:value (latest-observation "temperature" observations)))
                      :station "Vihreäsaari"}]
    (log/info "Current weather data: " weather-data)
    weather-data))