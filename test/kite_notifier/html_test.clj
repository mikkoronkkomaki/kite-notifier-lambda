(ns kite-notifier.html-test
  (:require
    [clojure.test :refer :all]
    [kite-notifier.html :as html]
    [clj-time.core :as t]))

(def weather-data {:wind-speed 7.5M
                   :wind-gust 8.5M
                   :wind-direction 180
                   :temperature 15
                   :time (t/date-time 2017 6 6 12 15)})

(deftest weather-html-document
  (let [document (html/weather-document weather-data)
        expected "<!DOCTYPE html><html><head><meta charset=\"UTF-8\" /><title>Oulu kitetiedot</title><link crossorigin=\"anonymous\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" rel=\"stylesheet\" /><script async=\"\" defer=\"\" src=\"https://buttons.github.io/buttons.js\"></script><style>#wrapper {\n    width: 800px;\n   }\n   #wrapper .container {\n    max-width: 100%;\n    display: block;\n    align: center;\n   }</style></head><div id=\"wrapper\"><div class=\"container\"><div id=\"header\"><h1>Oulu Kitetiedot</h1></div><div class=\"panel panel-success\"><div class=\"panel-heading\"><h3 class=\"panel-title\">null: 7.5 / 8.5 m/s &#8593; <span class=\"glyphicon glyphicon-thumbs-up\"></span></h3></div><div class=\"panel-body\"><div id=\"latest-observation\"><p>Lämpötila: 15 °C</p><p>Mitattu: 2017.06.06 15:15</p></div></div></div><div id=\"footer\"><p><a class=\"twitter-follow-button\" data-show-count=\"false\" href=\"https://twitter.com/OuluKiteTiedot\">Follow @OuluKiteTiedot</a><script>!function (d, s, id) {\n     var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';\n     if (!d.getElementById(id)) {\n        js = d.createElement(s);\n        js.id = id;\n        js.src = p + '://platform.twitter.com/widgets.js';\n        fjs.parentNode.insertBefore(js, fjs);\n      }\n    }(document, 'script', 'twitter-wjs');</script></p><p><a aria-label=\"Follow Kite Notifier Lambda on GitHub\" class=\"github-button\" href=\"https://github.com/mikkoronkkomaki/kite-notifier-lambda/\">Follow Kite Notifier Lambda</a></p></div></div></div></html>"]
    (is (= expected document))))
