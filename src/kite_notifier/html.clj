(ns kite-notifier.html
  (:require [hiccup.core :refer [html]]
            [kite-notifier.weather-data :as wd]
            [kite-notifier.s3 :as s3]
            [kite-notifier.date-time :as dt]))

(def css-style
  "#wrapper {
    width: 800px;
   }
   #wrapper .container {
    max-width: 100%;
    display: block;
    align: center;
   }")

(def twitter-script
  "!function (d, s, id) {
     var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';
     if (!d.getElementById(id)) {
        js = d.createElement(s);
        js.id = id;
        js.src = p + '://platform.twitter.com/widgets.js';
        fjs.parentNode.insertBefore(js, fjs);
      }
    }(document, 'script', 'twitter-wjs');")

(defn wind-direction-icon [wind-direction]
  (cond
    (wd/north? wind-direction) "&#8593;"
    (wd/north-east? wind-direction) "&#8601;"
    (wd/east? wind-direction) "&#8592;"
    (wd/south-east? wind-direction) "&#8598;"
    (wd/south? wind-direction) "&#8593;"
    (wd/south-west? wind-direction) "&#8599;"
    (wd/west? wind-direction) "&#8594;"
    (wd/north-west? wind-direction) "&#8600;"
    :else "?"))

(defn panel-class [wind-speed wind-gust wind-direction]
  (cond
    (wd/conditions-good? wind-speed wind-gust wind-direction) :div.panel.panel-success
    (or (wd/strong-gusts? wind-speed wind-gust) (wd/strong-wind? wind-speed)) :div.panel.panel-danger
    :else :div.panel.panel-primary))

(defn header []
  [:div#header
   [:h1 "Oulu Kitetiedot"]])

(defn footer []
  [:div#footer
   [:p
    [:a.twitter-follow-button
     {:href "https://twitter.com/OuluKiteTiedot"
      :data-show-count "false"} "Follow @OuluKiteTiedot"]
    [:script twitter-script]]
   [:p
    [:a.github-button
     {:href "https://github.com/mikkoronkkomaki/kite-notifier-lambda/"
      :aria-label "Follow Kite Notifier Lambda on GitHub"}
     "Follow Kite Notifier Lambda"]]])

(defn observations-from-station [{:keys [station wind-speed wind-gust wind-direction temperature time]}]
  [(panel-class wind-speed wind-gust wind-direction)
   [:div.panel-heading
    [:h3.panel-title (format "%s: %s / %s m/s %s "
                             station
                             wind-speed
                             wind-gust
                             (wind-direction-icon wind-direction))
     (when (wd/conditions-good? wind-speed wind-gust wind-direction)
       [:span.glyphicon.glyphicon-thumbs-up])
     (when (or (wd/strong-gusts? wind-speed wind-gust) (wd/strong-wind? wind-speed))
       [:span.glyphicon.glyphicon-ban-circle])]]
   [:div.panel-body
    [:div#latest-observation
     [:p (format "Lämpötila: %s °C" temperature)]
     [:p (format "Mitattu: %s" (dates/to-finnish-time time))]]]])

(defn weather-document-hiccup [weather-data]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "Oulu kitetiedot"]
    [:link
     {:rel "stylesheet"
      :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
      :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
      :crossorigin "anonymous"}]
    [:script
     {:async ""
      :defer ""
      :src "https://buttons.github.io/buttons.js"}]
    [:style css-style]]
   [:div#wrapper
    [:div.container
     (header)
     (observations-from-station weather-data)
     (footer)]]])

(defn weather-document [weather-data]
  (let [html-header "<!DOCTYPE html>"
        hiccup (weather-document-hiccup weather-data)]
    (str html-header (html hiccup))))

(defn publish [html-bucket weather-data]
  (s3/write-file-to-s3 html-bucket "index.html" (weather-document weather-data) "text/html" true))