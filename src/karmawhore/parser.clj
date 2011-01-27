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

(ns karmawhore.parser
  (:gen-class)
  (:use [clojure.contrib.duck-streams :only (read-lines reader)])
  (:use [clojure.contrib.seq :only (separate)])
  (:use [clojure.contrib.str-utils :only (re-sub)])
  (:use [clojure.contrib.generic.functor :only (fmap)])
  (:use [clojure.contrib.json :only (read-json)])
  (:use [karmawhore.color :only (bold red white green)]))

(def nick-vote #"(^|\s)([A-~][A-~\d]*)(\-\-|\+\+)")
;; the default configuration, assumed when no config file was found or
;; the configuration did not get loaded at all
(def config {:blacklist [] :join {}})

(defn blacklisted? [nick]
  (let [blacklist (config :blacklist)
        ;; convert the matches or non-matches (nil) to true or false
        predicate #(not (nil? (re-matches % nick)))
        tested (map predicate blacklist)]
    (some true? tested)))

(defn join-nick [mapping nick]
  (let [matches (fn [regexp] (re-matches regexp nick))
        criterion (fn [[candidate regexps]] (some matches regexps))
        possible-nicks (filter criterion mapping)]
    (if (empty? possible-nicks) nick
      (key (first possible-nicks)))))

(defn join-nick-list [nick-list]
  (let [mapping (config :join)
        joining-fn (partial join-nick mapping)]
    (map joining-fn nick-list)))

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

(defn- process-matches [match-list]
  (->> match-list
    (map first)
    (join-nick-list)
    (map normalize-nick)
    (remove blacklisted?)
    (frequencies)))

(defn match-line [line]
  (let [result (re-seq nick-vote line)]
    (if (nil? result) nil
      ;; drop the first two elements, because they contain useless groups
      (map (partial drop 2) result))))

(defn get-votes [line]
  (let [matches (match-line line)
        [up down] (separate #(= (second %) "++") matches)
        upvotes (process-matches up)
        downvotes (process-matches down)]
    (into {}
          (for [user (set (mapcat keys [upvotes downvotes]))]
            {user {:upvotes (get upvotes user 0)
                   :downvotes (get downvotes user 0)}}))))

;; of course I ran into an error in clojure 1.2 fixed in 1.3
;; http://dev.clojure.org/jira/browse/CONTRIB-99
;; http://dev.clojure.org/jira/browse/CONTRIB-101
;; This is why, for now, we only run read-json when the file exists until
;; Clojure 1.3 gets released
(defn load-config []
  (let [conf (try
               (let [json (read-json (slurp "karmawhore.json"))
                     ;; convert items into regexp
                     processed-blacklist (->>
                                           (json :blacklist)
                                           (map re-pattern)
                                           (assoc json :blacklist))
                     ;; convert into set of regexp
                     processed-join (->>
                                      (processed-blacklist :join)
                                      (map (fn [[k v]] [(name k) (set (map re-pattern v))]))
                                      (into {})
                                      (assoc processed-blacklist :join))]
                 processed-join)
               (catch java.io.FileNotFoundException e config))]
    conf))

(defn -main [& args]
  ;; make the config locally known using dynamic binding
  (binding [config (load-config)]
    (let [file-name (first args)
          line-votes (map get-votes (read-lines file-name))
          votes (reduce (fn [a b] (merge-with (partial merge-with +) a b)) line-votes)
          summed-karma (for [[k {u :upvotes d :downvotes}] votes] [k {:upvotes u :downvotes d :sum (- u d)}])
          sorted-by-karma (sort-by (comp - :sum second) summed-karma)]
      (doseq [[nick {u :upvotes d :downvotes s :sum}] sorted-by-karma]
        (printf "%s: Karma %s (Upvotes %s, Downvotes %s)\n"
                (white nick) (bold (white s)) (green u) (red d))))))
