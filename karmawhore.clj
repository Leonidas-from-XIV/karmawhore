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
  (:use [clojure.contrib.duck-streams :only (read-lines)]))

(def allowed-nickname "[A-z]{1,16}")
(def nick-plus (re-pattern (format "(%s)\\+\\+" allowed-nickname)))
(def nick-minus (re-pattern (format "(%s)\\-\\-" allowed-nickname)))

(defn extract-nicks [regexp line]
  (map second (re-seq regexp line)))

(defn modify-karma [op h nick]
  (let [current-value (h nick)]
    (if (nil? current-value) (assoc h nick (op 1))
      (assoc h nick (op current-value 1)))))

(def increase-karma (partial modify-karma +))
(def decrease-karma (partial modify-karma -))

(defn process-line [acc line]
  (let [nicks-add (extract-nicks nick-plus line)
        nicks-sub (extract-nicks nick-minus line)
        after-add (reduce increase-karma acc nicks-add)]
    (reduce decrease-karma after-add nicks-sub)))

(defn -main [& args]
  (let [file-name (first (first args))
        histogram (reduce process-line (hash-map) (read-lines file-name))
        nonzero? (comp not zero? second)
        histogram (filter nonzero? histogram)
        sorted-by-karma (sort-by #(- (second %)) histogram)]
    (doseq [item sorted-by-karma]
      (printf "%s %d\n" (first item) (second item)))))

(-main *command-line-args*)
