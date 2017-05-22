(ns kite-notifier.date-time-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.date-time :as dt]
    [clj-time.core :as t])
  (:use org.httpkit.fake))

(deftest to-finnish-time
  (is (= "2017.06.06 19:16" (dt/to-finnish-time (t/date-time 2017 6 6 16 16)))))

(deftest parse
  (is (= (t/date-time 2017 5 22 9 20) (dt/parse "2017-05-22T09:20:00Z"))))

(deftest hours-ago
  (with-redefs [t/now #(t/date-time 2017 6 6 16 16)]
    (is (= 4 (dt/hours-ago (t/date-time 2017 6 6 12 16))))))

(deftest quiet-time?
  (with-redefs [t/now #(t/date-time 2017 6 6 16)]
    (is (not (dt/quiet-time?))))
  (with-redefs [t/now #(t/date-time 2017 6 6 22)]
    (is (dt/quiet-time?)))
  (with-redefs [t/now #(t/date-time 2017 6 6 3)]
    (is (dt/quiet-time?))))

