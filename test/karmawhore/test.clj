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
  (is (["Leonidas++" "Leonidas"] "Simple upvote for Leonidas++"))
  (is (["Leonidas--" "Leonidas"] "Equally simple Leonidas-- downvote")))
