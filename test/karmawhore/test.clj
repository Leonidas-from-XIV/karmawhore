(ns karmawhore.test
  (:use [karmawhore.parser] :reload)
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

(deftest blacklisting-nicks
  (binding [config {:blacklist [#"Leonidas" #".*enefungus"]
                    :join #{}}]
    (are [parsed line] (= parsed (get-votes line))
         {} "Leonidas++ is blacklisted, though"
         {} "Xenefungus++ is also blacklisted"
         {} "Zombiexenefungus-- wouldn't get any karma"

         {"larsrh" {:upvotes 1 :downvotes 0}}
         "Edeltraudzombiexenefungus-- gets no karma, larsrh++ does")))

(deftest joining-nicks
  (binding [config {:blacklist []
                    :join {"Xenefungus" #{#"Zombiexenefungus" #"Edeltraudzombiexenefungus"}
                           "x127" #{#".127"}}}]
    (are [parsed line] (= parsed (get-votes line))
         {"Xenefungus" {:upvotes 1 :downvotes 0}}
         "Zombiexenefungus++ gets karma for zombie prefix"

         {"x127" {:upvotes 2 :downvotes 0}}
         "x127++ and {127++ is actually the same"

         {"Xenefungus" {:upvotes 0 :downvotes 1} "x127" {:upvotes 1 :downvotes 0}}
         "Edeltraudzombiexenefungus-- for long nick, {127++ for strange characters"

         {"x127" {:upvotes 1 :downvotes 1}}
         "x127++ for short nick, {127-- for two nicks in channel")))

(deftest normalize-join-blacklist-nicks
  (binding [config {:blacklist [#"Leonidas", #"Xenefungus"]
                    :join {"Xenefungus" #{#".*enefungus"}}}]
    (are [parsed line] (= parsed (get-votes line))
         {} "Leonidas++ is directly in the blacklist"
         {} "Leonidas_++ is only normalized in the blacklist"
         {} "Zombiexenefungus-- is not, but the joined version is"

         {"x127" {:upvotes 1 :downvotes 0}}
         "Leonidas|away-- is indirectly blocklisted, x127++ not"

         {"x127" {:upvotes 0 :downvotes 1}}
         "x127|away-- for using away-nicks"

         {"x127" {:upvotes 2 :downvotes 1}}
         "x127_++ with postfix, _x127-- with prefix, _x127_++ with both")))

;; this test might be a bit too implementation specific, the regex-matcher
;; should only output relevant stuff
(deftest regex-matches
  (are [matched line] (= matched (match-line line))
       '(["Leonidas" "++"]) "Simple upvote for Leonidas++"
       '(["Leonidas" "--"]) "Equally simple Leonidas-- downvote"
       ; two matches: upvote and downvote
       '(["Leonidas" "--"] ["Leonidas" "++"])
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
