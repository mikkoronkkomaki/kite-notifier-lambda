(ns kite-notifier.weather-data)

(defn between? [min max val]
  (and (<= min val) (>= max val)))

(defn north-east? [wind-direction] (between? 22.5M 67.5M wind-direction))
(defn east? [wind-direction] (between? 67.5M 112.5M wind-direction))
(defn south-east? [wind-direction] (between? 112.5M 157.5M wind-direction))
(defn south? [wind-direction] (between? 157.5M 202.5M wind-direction))
(defn south-west? [wind-direction] (between? 202.5M 247.5M wind-direction))
(defn west? [wind-direction] (between? 247.5M 292.5M wind-direction))
(defn north-west? [wind-direction] (between? 292.5M 337.5M wind-direction))
(defn north? [wind-direction] (or (between? 337.5M 360 wind-direction)
                                  (between? 0 22.5M wind-direction)))

(defn conditions-good? [wind-speed wind-gust wind-direction]
  (and (between? 7 15 wind-speed)
       (between? 7 16 wind-gust)
       (> 4 (- wind-gust wind-speed))
       (or (south? wind-direction)
           (south-west? wind-direction)
           (west? wind-direction)
           (north-west? wind-direction)
           (north? wind-direction))))

(defn strong-wind? [wind-speed]
  (< 15 wind-speed))

(defn strong-gusts? [wind-speed wind-gust]
  (<= 4 (- wind-gust wind-speed)))

(defn wind-direction-explanation [wind-direction]
  (if wind-direction
    (cond
      (north-east? wind-direction) "Koilinen"
      (east? wind-direction) "Itä"
      (south-east? wind-direction) "Kaakko"
      (south? wind-direction) "Etelä"
      (south-west? wind-direction) "Lounas"
      (west? wind-direction) "Länsi"
      (north-west? wind-direction) "Luode"
      (north? wind-direction) "Pohjoinen"
      :else "Suunta ei saatavilla")
    "Suunta ei saatavilla"))
