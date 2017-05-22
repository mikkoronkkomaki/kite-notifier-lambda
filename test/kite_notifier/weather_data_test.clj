(ns kite-notifier.weather-data-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.weather-data :as weather-data]))

(deftest between
  (is (weather-data/between? 1 3 2))
  (is (weather-data/between? 1 2 2))
  (is (weather-data/between? 1 2 1))
  (is (weather-data/between? 1 1 1))
  (is (not (weather-data/between? 1 3 0)))
  (is (not (weather-data/between? 1 3 4))))

(deftest good-conditions
  (is (weather-data/conditions-good? 7 7.5M 180) "Good conditions are considered good")
  (is (not (weather-data/conditions-good? 7 7.5M 70)) "Wrong wind direction (east) is not considered good")
  (is (not (weather-data/conditions-good? 6.9M 7.5M 70)) "Too little wind is not considered good")
  (is (not (weather-data/conditions-good? 16.1M 17 70)) "Too strong wind is not considered good")
  (is (not (weather-data/conditions-good? 12.1M 17 70)) "Too strong wind gusts are not considered good"))

(deftest wind-direction-explanation
  (is (= "Etelä" (weather-data/wind-direction-explanation 180)))
  (is (= "Pohjoinen" (weather-data/wind-direction-explanation 360)))
  (is (= "Itä" (weather-data/wind-direction-explanation 90)))
  (is (= "Länsi" (weather-data/wind-direction-explanation 270)))
  (is (= "Koilinen" (weather-data/wind-direction-explanation 23)))
  (is (= "Koilinen" (weather-data/wind-direction-explanation 60)))
  (is (= "Lounas" (weather-data/wind-direction-explanation 205)))
  (is (= "Luode" (weather-data/wind-direction-explanation 300))))


