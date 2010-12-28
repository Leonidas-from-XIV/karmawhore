(ns karmawhore.test
  (:use [karmawhore] :reload)
  (:use [clojure.test]))

;(def a {"Leonidas" {:upvotes 1 :downvotes 2}})
;(def b {"Leonidas" {:upvotes 4 :downvotes 1}})
;(merge-with (partial merge-with +) a b)
(deftest parses-votes
  (is {"Leonidas" {:upvotes 1 :downvotes 0}}
      (get-votes "hey, Leondidas++ for writing Karmawhore"))
  (is {"Leonidas" {:upvotes 2 :downvotes 0}}
      (get-votes "Leonidas++ gets a lot of karma, Leonidas++"))
  (is {"Leonidas" {:upvotes 1 :downvotes 0} "Xenefungus" {:upvotes 0 :downvotes 1}}
      (get-votes "Xenefungus-- gets no karma but Leonidas++ does"))
  (is {} (get-votes "Noone gets karma"))
  (is {} (get-votes "URLs like http://in.tum.de/?foo=bar++baz don't have karma"))
  (is {"Xenefungus" {:upvotes 0 :downvotes 1}} (get-votes "Xenefungus-- uses Windows")))

(deftest regex-matches
  (are [matched line] (= matched (match-line line))
       '(["Leonidas++" "Leonidas" "++"]) "Simple upvote for Leonidas++"
       '(["Leonidas--" "Leonidas" "--"]) "Equally simple Leonidas-- downvote"
       ; two matches: upvote and downvote
       '(["Leonidas--" "Leonidas" "--"] ["Leonidas++" "Leonidas" "++"])
       "Downvoting Leonidas-- and upvoting Leonidas++ again"
       ; no matches
       nil "No votes on this line"
       nil "No votes+ + on this one either"
       nil "Numbers cannot be upvoted like this: 42++ 23--"))

(deftest normalize-nicks
  (are [clean dirty] (= clean (normalize-nick dirty))
       "Leonidas" "Leonidas"
       "Leonidas" "Leonidas_"
       "Leonidas" "Leonidas|away"
       "Leonidas" "Leonidas`away"
       "Leonidas" "[Clan]Leonidas"))

(deftest join-nicks-by-regex
  (are [clean mapping dirty] (= clean (join-nick mapping dirty))
       "Leonidas" {"Leonidas" '(#"Leonidas\S*")} "LeonidasFoobar"))
