(ns kite-notifier.pushover
  (:require [org.httpkit.client :as http]
            [kite-notifier.weather-data :refer [conditions-good? strong-wind? strong-gusts? wind-direction-explanation]]
            [clj-time.format :as f]
            [taoensso.timbre :as log]
            [kite-notifier.dates :as dates]))

(defn notification [{:keys [time wind-speed wind-gust wind-direction temperature] :as weather-data} station]
  (let [title (cond (strong-wind? wind-speed) (format "Varoitus! Kova tuuli mitattu asemalla: %s." station)
                    (strong-gusts? wind-speed wind-gust) (format "Varoitus! Kovia puuskia mitattu asemalla: %s." station)
                    (conditions-good? wind-speed wind-gust wind-direction) (format "Keli päällä asemalla: %s." station))
        wind-direction (wind-direction-explanation wind-direction)
        time (dates/to-finnish-time time)
        message (format "Mitattu: %s, tuulen nopeus %s m/s, puuskat: %s m/s, suunta: %s, lämpötila: %s °C"
                        time wind-speed wind-gust wind-direction temperature)]
    {:title title
     :message message}))

(defn send-notification [token user weather-data station]
  (let [{message :message title :title} (notification weather-data station)
        options {:form-params {:token token
                               :user user
                               :message message
                               :title title
                               :url "https://pushover.net/api"}}
        {:keys [status error body]} @(http/post "https://api.pushover.net/1/messages.json" options)]
    (log/info "Response: " status ", " error ", " body)))