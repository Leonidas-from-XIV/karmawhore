(ns karmawhore.color
  (:use [clojure.contrib.str-utils :only (str-join)]))

;; sometimes we want to override the TTY detection
(def *force-color* false)

;; template that will be filled in with the appropriate color values
(def sgr-template "\u001b[%dm")

(def sgr-colors
  {:reset 0
   :bold 1
   :red 31
   :green 32
   :yellow 33
   :blue 34
   :magenta 35
   :cyan 36
   :white 37})

;; http://stackoverflow.com/questions/1403772/
;(defn is-a-tty? []
;  (not (nil? (.. System console))))

(def is-a-tty? (not (nil? (.. System console))))

(defn- fill-color [color]
  ;; only colorize when a TTY is connected
  (if (or is-a-tty? *force-color*) (format sgr-template (sgr-colors color))
    ""))

(defn colorize [color text]
  (let [color (fill-color color)
        rst (fill-color :reset)]
    (str-join "" [color text rst])))

(def bold (partial colorize :bold))
(def red (partial colorize :red))
(def green (partial colorize :green))
(def yellow (partial colorize :yellow))
(def blue (partial colorize :blue))
(def magenta (partial colorize :magenta))
(def cyan (partial colorize :cyan))
(def white (partial colorize :white))
