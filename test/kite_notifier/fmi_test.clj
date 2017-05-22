(ns kite-notifier.fmi-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.fmi :as fmi]
    [clj-time.core :as t])
  (:use org.httpkit.fake))

(deftest fmi-weather-data
  (let [api-key "666"
        station-id "666"
        url (str "http://data.fmi.fi/fmi-apikey/"
                 api-key
                 "/wfs?request=getFeature&storedquery_id=fmi::observations::weather::simple&fmisid="
                 station-id
                 "&parameters=windspeedms,winddirection,windGust,temperature")]

    (with-fake-http [url (slurp "test/resources/fmi.xml")]
      (let [expected {:time (t/date-time 2017 5 22 15 40)
                      :wind-speed 7.3
                      :wind-gust 7.7
                      :wind-direction 320.0
                      :temperature 8.2
                      :station "Vihreäsaari"}
            weather-data (fmi/get-weather-data api-key station-id)]
        (is (= expected weather-data))))))
