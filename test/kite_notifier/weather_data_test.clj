(ns kite-notifier.weather-data-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.weather-data :as weather-data]))

(deftest good-conditions
  (is (weather-data/conditions-good? 7 7.5M 180) "Good conditions are considered good")
  (is (not (weather-data/conditions-good? 7 7.5M 70)) "Wrong wind direction (east) is not considered good")
  (is (not (weather-data/conditions-good? 6.9M 7.5M 70)) "Too little wind is not considered good")
  (is (not (weather-data/conditions-good? 16.1M 17 70)) "Too strong wind is not considered good")
  (is (not (weather-data/conditions-good? 12.1M 17 70)) "Too strong wind gusts are not considered good"))
