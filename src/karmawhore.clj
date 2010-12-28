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

(ns karmawhore
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (read-lines)])
  (:use [clojure.contrib.seq :only (separate)])
  (:use [clojure.contrib.str-utils :only (re-sub)])
  (:use [clojure.contrib.generic.functor :only (fmap)]))

(def allowed-nickname "[A-~\\d]{1,16}")
(def nick-plus (re-pattern (format "(%s)\\+\\+" allowed-nickname)))
(def nick-minus (re-pattern (format "(%s)\\-\\-" allowed-nickname)))
(def nick-vote #"([A-~][A-~\d]*)(\-\-|\+\+)")

(defn get-votes [line]
  (let [matches (re-seq nick-vote line)
        [up down] (separate #(= (nth % 2) "++") matches)
        upvotes (frequencies (map second up))
        downvotes (frequencies (map second down))]
    (into {}
          (for [user (set (mapcat keys [upvotes downvotes]))]
            {user {:upvotes (get upvotes user 0)
                   :downvotes (get downvotes user 0)}}))))

(defn get-histogram [line]
  (let [upvotes (get-votes nick-plus line)
        downvotes (fmap - (get-votes nick-minus line))]
    (merge-with + upvotes downvotes)))

(defn match-line [line]
  (re-seq nick-vote line))

(defn normalize-nick [nick]
  (let [eliminate '(
                    ; remove underscores at the beginning and end
                    #"_+$" #"^_+"
                    ; remove stuff in square brackets
                    #"\[.*?\]"
                    ; remove "appended" shit
                    #"[`|].*$"
                    )]
    (reduce (fn [n regexp] (re-sub regexp "" n)) nick eliminate)))

(defn join-nick [mapping nick]
  nick)

(defn -main [& args]
  (let [file-name (first args)
        histograms (map get-histogram (read-lines file-name))
        histogram (apply merge-with + histograms)
        histogram (remove (comp zero? second) histogram)
        sorted-by-karma (reverse (sort-by second histogram))]
    (doseq [[nick karma] sorted-by-karma]
      (println nick karma))))
