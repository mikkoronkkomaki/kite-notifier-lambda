(ns kite-notifier.fmi
  (:require [clojure.data.zip.xml :as z]
            [clojure.zip :refer [xml-zip]]
            [clojure.xml :refer [parse]]
            [org.httpkit.client :as http]
            [clj-time.format :as f])
  (:import (java.io ByteArrayInputStream)
           (org.xml.sax SAXParseException)))

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
        weather-data {:time (f/parse(f/formatter "yyyy-MM-dd'T'HH:mm:ssZ")
                                    (:time (latest-observation "windspeedms" observations)))
                      :wind-speed (Double/parseDouble (:value (latest-observation "windspeedms" observations)))
                      :wind-gust (Double/parseDouble (:value (latest-observation "windGust" observations)))
                      :wind-direction (Double/parseDouble (:value (latest-observation "winddirection" observations)))
                      :temperature (Double/parseDouble (:value (latest-observation "temperature" observations)))}]
    (println "Current weather data: " weather-data)
    weather-data))