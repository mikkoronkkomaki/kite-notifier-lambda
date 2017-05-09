(ns kite-notifier.dates
  (:require [clj-time.core :as time]
            [clj-time.format :as format]))

(defn to-finnish-time [time]
  (format/unparse (format/with-zone
                    (format/formatter "yyyy.MM.dd HH:mm")
                    (time/time-zone-for-id "Europe/Helsinki"))
                  time))

(defn parse [value]
  (format/parse (format/formatter "yyyy-MM-dd'T'HH:mm:ssZ") value))