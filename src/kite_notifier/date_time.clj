(ns kite-notifier.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as format]
            [clj-time.core :as t])
  (:import (org.joda.time DateTimeZone)))

(defn to-finnish-time [time]
  (format/unparse (format/with-zone
                    (format/formatter "yyyy.MM.dd HH:mm")
                    (time/time-zone-for-id "Europe/Helsinki"))
                  time))

(defn parse [value]
  (format/parse (format/formatter "yyyy-MM-dd'T'HH:mm:ssZ") value))

(defn hours-ago [time]
  (t/in-hours (t/interval time (t/now))))

(defn quiet-time? []
  (let [current-hour (t/hour (t/to-time-zone (t/now) (DateTimeZone/forID "Europe/Helsinki")))]
    (and (> 7 current-hour) (< 22 current-hour))))