(ns karmawhore.test
  (:use [karmawhore] :reload)
  (:use [clojure.test]))

(deftest parses-simple-votes
  (is {"Leonidas" 1} (get-upvotes "hey, Leondidas++ for writing Karmawhore")))
