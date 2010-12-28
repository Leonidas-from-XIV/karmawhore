(ns karmawhore.test
  (:use [karmawhore] :reload)
  (:use [clojure.test]))

;(def a {"Leonidas" {:upvotes 1 :downvotes 2}})
;(def b {"Leonidas" {:upvotes 4 :downvotes 1}})
;(merge-with (partial merge-with +) a b)
(deftest parses-votes
  (are [parsed line] (= parsed (get-votes line))
       {"Leonidas" {:upvotes 1 :downvotes 0}}
       "hey, Leonidas++ for writing Karmawhore"

       {"Leonidas" {:upvotes 2 :downvotes 0}}
       "Leonidas++ gets a lot of karma, Leonidas++"

       {"Leonidas" {:upvotes 1 :downvotes 0} "Xenefungus" {:upvotes 0 :downvotes 1}}
       "Xenefungus-- gets no karma but Leonidas++ does"

       {} "Noone gets karma"
       {} "URLs like http://in.tum.de/?foo=bar++baz don't have karma"
       {"Xenefungus" {:upvotes 0 :downvotes 1}} "Xenefungus-- uses Windows"))

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
       ; a "basename" nick should evaluate to itself
       "Leonidas" "Leonidas"
       "Leonidas" "Leonidas_"
       "Leonidas" "Leonidas__"
       "Leonidas" "_Leonidas"
       "Leonidas" "__Leonidas"
       "Leonidas" "Leonidas|away"
       "Leonidas" "Leonidas`away"
       "Leonidas" "[Clan]Leonidas"))

(deftest join-nicks-by-regex
  (are [clean mapping dirty] (= clean (join-nick mapping dirty))
       "Leonidas" {"Leonidas" #{#"Leonidas\S*"}} "LeonidasFoobar"))
