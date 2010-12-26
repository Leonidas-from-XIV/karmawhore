(ns karmawhore.test
  (:use [karmawhore] :reload)
  (:use [clojure.test]))

;(def a {"Leonidas" {:upvotes 1 :downvotes 2}})
;(def b {"Leonidas" {:upvotes 4 :downvotes 1}})
;(merge-with (partial merge-with +) a b)
(deftest parses-votes
  (is {"Leonidas" 1} (get-upvotes "hey, Leondidas++ for writing Karmawhore"))
  (is {"Leonidas" 2} (get-upvotes "Leonidas++ gets a lot of karma, Leonidas++"))
  (is {"Leonidas" 1} (get-upvotes "Xenefungus-- gets no karma but Leonidas++ does"))
  (is {} (get-upvotes "Noone gets karma"))
  (is {} (get-upvotes "URLs like http://in.tum.de/?foo=bar++baz don't have karma"))
  (is {"Xenefungus" 1} (get-downvotes "Xenefungus-- uses Windows"))
  (is {} (get-downvotes "Everyone is fine")))

(deftest regex-matches
  (is '(["Leonidas++" "Leonidas"])
      (match-line "Simple upvote for Leonidas++"))
  (is '(["Leonidas--" "Leonidas"])
      (match-line "Equally simple Leonidas-- downvote"))
  (is '(["Leonidas--" "Leonidas"] ["Leonidas++" "Leonidas"])
      (match-line "Downvoting Leonidas-- and upvoting Leonidas++ again"))
  (is nil (match-line "No votes on this line"))
  (is nil (match-line "No votes+ + on this one either"))
  (is nil (match-line "Numbers cannot be upvoted like this: 42++ 23--")))

(deftest normalize-nicks
  (are [clean dirty] (= clean (normalize-nick dirty))
       "Leonidas" "Leonidas"
       "Leonidas" "Leonidas_"
       "Leonidas" "Leonidas|away"
       "Leonidas" "Leonidas`away"
       "Leonidas" "[Clan]Leonidas"))

(deftest join-nicks-by-regex
  (are [clean mapping dirty] (= clean (join-nicks mapping dirty))
       "Leonidas" {"Leonidas" '(#"Leonidas\S*")} "LeonidasFoobar"))
