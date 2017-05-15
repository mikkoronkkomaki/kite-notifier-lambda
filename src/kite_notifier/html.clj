(ns kite-notifier.html
  (:require [hiccup.core :refer [html]]
            [kite-notifier.weather-data :refer [wind-direction-explanation]]
            [kite-notifier.s3 :as s3]))

(defn header []
  [:div#header
   [:h1 "Oulu kitetiedot"]])

(defn footer []
  [:div#footer
   [:p
    [:a.twitter-follow-button
     {:href "https://twitter.com/OuluKiteTiedot", :data-show-count "false"}
     "Follow
                 @OuluKiteTiedot"]
    [:script
     "!function (d, s, id) {
                     var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';
                                     if (!d.getElementById(id)) {\n                    js = d.createElement(s);\n                    js.id = id;\n                    js.src = p + '://platform.twitter.com/widgets.js';\n                    fjs.parentNode.insertBefore(js, fjs);\n                }\n            }(document, 'script', 'twitter-wjs');"]]
   [:p
    [:a.github-button
     {:href "https://github.com/mikkoronkkomaki/kite-notifier-lambda/", :aria-label "Follow Kite Notifier Lambda on GitHub"}
     "Follow Kite Notifier Lambda"]]])

(defn observations-from-station [{:keys [station wind-speed wind-gust wind-direction temperature time]}]
  (let [wind-direction (wind-direction-explanation wind-direction)]
    [:div.panel.panel-default
     [:div.panel-heading
      [:h3.panel-title
       "Vihreäsaari"]]
     [:div.panel-body
      [:div#latest-observation
       [:ul
        [:li (format "Tuuli: %s %s/%s m/s" wind-direction wind-speed wind-gust)]
        [:li (format "Lämpötila: %s °C" temperature)]
        [:li (format "Mitattu: %s" time)]]]]]))

(defn weather-document [weather-data]
  (let [html-header "<!DOCTYPE html>
                      <html lang=\"en\">
                      <head>
                        <meta charset=\"UTF-8\">
                        <title>Oulu kitetiedot</title>
                        <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\"
                        integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">
                        <script async defer src=\"https://buttons.github.io/buttons.js\"></script>
                      </head>"
        document [:div#container
                  (header)
                  (observations-from-station weather-data)
                  (footer)]]
    (str html-header (html document))))

(defn publish [html-bucket weather-data]
  (s3/write-file-to-s3 html-bucket "index.html" (weather-document weather-data) "text/html"))