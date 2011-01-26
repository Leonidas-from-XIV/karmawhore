;;; Karmawhore - an IRC karma tracker script
;;; Copyright (C) 2010, 2011  Leonidas
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
  (:use [clojure.contrib.duck-streams :only (read-lines reader)])
  (:use [clojure.contrib.seq :only (separate)])
  (:use [clojure.contrib.str-utils :only (re-sub)])
  (:use [clojure.contrib.generic.functor :only (fmap)])
  (:use [clojure.contrib.json :only (read-json)]))

(def nick-vote #"([A-~][A-~\d]*)(\-\-|\+\+)")
;; the default configuration, assumed when no config file was found or
;; the configuration did not get loaded at all
(def config {:blacklist []})

(defn get-votes [line]
  (let [matches (re-seq nick-vote line)
        [up down] (separate #(= (nth % 2) "++") matches)
        upvotes (frequencies (map second up))
        downvotes (frequencies (map second down))]
    (into {}
          (for [user (set (mapcat keys [upvotes downvotes]))]
            {user {:upvotes (get upvotes user 0)
                   :downvotes (get downvotes user 0)}}))))

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
  (let [matches (fn [regexp] (re-matches regexp nick))
        criterion (fn [[candidate regexps]] (some matches regexps))]
    (key (first (filter criterion mapping)))))

;; of course I ran into an error in clojure 1.2 fixed in 1.3
;; http://dev.clojure.org/jira/browse/CONTRIB-99
;; http://dev.clojure.org/jira/browse/CONTRIB-101
;; This is why, for now, we only run read-json when the file exists until
;; Clojure 1.3 gets released
(defn load-config []
  (let [conf (try
               (let [json (read-json (slurp "karmawhore.json"))]
                 ;; convert items into regexp
                 (assoc json :blacklist (map re-pattern (json :blacklist))))
               (catch java.io.FileNotFoundException e config))]
    conf))

(defn blacklisted? [item]
  (let [blacklist (config :blacklist)
        ;; convert the matches or non-matches (nil) to true or false
        predicate #(not (nil? (re-matches % item)))
        tested (map predicate blacklist)]
    (some true? tested)))

(defn -main [& args]
  ;; make the config locally known using dynamic binding
  (binding [config (load-config)]
    (let [file-name (first args)
          get-votes-and-filter #(->> % (get-votes) (remove (comp blacklisted? key)) (into {}))
          line-votes (map get-votes-and-filter (read-lines file-name))
          votes (reduce (fn [a b] (merge-with (partial merge-with +) a b)) line-votes)
          summed-karma (for [[k {u :upvotes d :downvotes}] votes] [k {:upvotes u :downvotes d :sum (- u d)}])
          sorted-by-karma (sort-by (comp - :sum second) summed-karma)]
      (doseq [[nick {u :upvotes d :downvotes s :sum}] sorted-by-karma]
        (printf "%s: Karma %d (Upvotes %d, Downvotes %d)\n" nick s u d)))))
