(ns kite-notifier.weather-data)

(defn between? [min max val]
  (and (<= min val) (>= max val)))

(defn conditions-good? [wind-speed wind-gust wind-direction]
  (and (between? 7 15 wind-speed)
       (between? 7 16 wind-gust)
       (> 4 (- wind-gust wind-speed))
       (between? 180 360 wind-direction)))

(defn strong-wind? [wind-speed]
  (< 15 wind-speed))

(defn strong-gusts? [wind-speed wind-gust]
  (<= 4 (- wind-gust wind-speed)))

(defn wind-direction-explanation [wind-direction]
  (cond
    (between? 22.5M 67.5M wind-direction) "Koilinen"
    (between? 67.5M 112.5M wind-direction) "Itä"
    (between? 112.5M 157.5M wind-direction) "Kaakko"
    (between? 157.5M 202.5M wind-direction) "Etelä"
    (between? 202.5M 247.5M wind-direction) "Lounas"
    (between? 247.5M 292.5M wind-direction) "Länsi"
    (between? 292.5M 337.5M wind-direction) "Luode"
    (between? 337.5M 22.5M wind-direction) "Luode"
    :else "Suunta ei saatavilla"))
