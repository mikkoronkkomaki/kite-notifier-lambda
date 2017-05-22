(ns kite-notifier.core-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.core :as c]
    [clj-time.core :as t])
  (:use org.httpkit.fake))

(deftest notification-allowed?
  (with-redefs [t/now #(t/date-time 2017 6 6 16)]
    (is (c/notification-allowed? (t/date-time 2017 6 6 9)))
    (is (not (c/notification-allowed? (t/date-time 2017 6 6 10))))))

