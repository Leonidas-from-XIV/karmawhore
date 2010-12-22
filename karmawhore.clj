;;; Karmawhore - an IRC karma tracker script
;;; Copyright (C) 2010  Leonidas
;;;
;;; This program is free software: you can redistribute it and/or modify
;;; it under the terms of the GNU Affero General Public License as published by
;;; the Free Software Foundation, either version 3 of the License, or
;;; (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU Affero General Public License for more details.
;;;
;;; You should have received a copy of the GNU Affero General Public License
;;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns net.xivilization.karmawhore
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [clojure.contrib.generic.functor :only (fmap)]))

(def allowed-nickname "[A-z]{1,16}")
(def nick-plus (re-pattern (format "(%s)\\+\\+" allowed-nickname)))
(def nick-minus (re-pattern (format "(%s)\\-\\-" allowed-nickname)))

(defn get-votes [regexp line]
  (let [nicks (map second (re-seq regexp line))]
    (frequencies nicks)))

(defn get-histogram [line]
  (let [upvotes (get-votes nick-plus line)
        downvotes (fmap - (get-votes nick-minus line))]
    (merge-with + upvotes downvotes)))

(defn -main [& args]
  (let [file-name (ffirst args)
        histograms (map get-histogram (read-lines file-name))
        histogram (apply merge-with + histograms)
        histogram (remove (comp zero? second) histogram)
        sorted-by-karma (reverse (sort-by second histogram))]
    (doseq [[nick karma] sorted-by-karma]
      (println nick karma))))

(-main *command-line-args*)
