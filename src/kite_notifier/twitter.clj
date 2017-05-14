(ns kite-notifier.twitter
  (:require [kite-notifier.weather-data :refer [conditions-good? strong-wind? strong-gusts? wind-direction-explanation]]
            [kite-notifier.dates :as dates])
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

(defn credentials [app-consumer-key
                   app-consumer-secret
                   user-access-token
                   user-access-token-secret]
  (make-oauth-creds app-consumer-key
                    app-consumer-secret
                    user-access-token
                    user-access-token-secret))

(defn status [{:keys [time wind-speed wind-gust wind-direction temperature station] :as weather-data}]
  (let [title (cond (strong-wind? wind-speed) (format "Varoitus: Kova tuuli (%s)!" station)
                    (strong-gusts? wind-speed wind-gust) (format "Varoitus: Kovat puuskat(%s)!" station)
                    (conditions-good? wind-speed wind-gust wind-direction) (format "Keli päällä (%s)!" station))
        wind-direction (wind-direction-explanation wind-direction)
        time (dates/to-finnish-time time)
        status (format "%s %s/%s m/s, %s, %s°C (%s)" title wind-speed wind-gust wind-direction temperature time)]
    status))

(defn post [app-consumer-key
            app-consumer-secret
            user-access-token
            user-access-token-secret
            weather-data]
  (let [status (status weather-data)
        credentials (credentials app-consumer-key
                                 app-consumer-secret
                                 user-access-token
                                 user-access-token-secret)]
    (statuses-update :oauth-creds credentials
                     :params {:status status})))