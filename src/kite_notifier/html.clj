(ns kite-notifier.html
  (:require [hiccup.core :refer [html]]
            [kite-notifier.weather-data :refer [wind-direction-explanation]]
            [kite-notifier.s3 :as s3]))

(defn header []
  [:div#header
   [:h1 "Oulu kitetiedot"]])

(defn footer []
  [:div#footer
   [:p [:a {:href "https://twitter.com/OuluKiteTiedot"} "Twitter"]]
   [:p [:a {:href "https://github.com/mikkoronkkomaki/kite-notifier-lambda"} "GitHub"]]])

(defn observations-from-station [{:keys [station wind-speed wind-gust wind-direction temperature time]}]
  (let [wind-direction (wind-direction-explanation wind-direction)]
    [:div#station
     [:div#station-title
      [:h2 station]]
     [:div#latest-observation
      [:ul
       [:li (format "Tuuli: %s %s/%s m/s" wind-direction wind-speed wind-gust)]
       [:li (format "Lämpötila: %s °C" temperature)]
       [:li (format "Mitattu: %s" time)]]]]))

(defn weather-document [weather-data]
  (let [html-header "<!DOCTYPE html>\n<html lang=\"en\">\n\n<head>\n    <meta charset=\"UTF-8\">\n    <title>Oulu kitetiedot</title>\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>\n</head>"
        document [:div#container
                  (header)
                  (observations-from-station weather-data)
                  (footer)]]
    (str html-header (html document))))

(defn publish [html-bucket weather-data]
  (s3/write-file-to-s3 html-bucket "index.html" (weather-document weather-data)))
