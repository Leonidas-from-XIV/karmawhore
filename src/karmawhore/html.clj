(ns karmawhore.html
  (:use [hiccup.core :only (html)]))

(defn record-row [record]
  (let [[nick {u :upvotes d :downvotes s :sum}] record]
    [:tr
     [:td.karmawhore-nick nick]
     [:td.karmawhore-upvotes (str u)]
     [:td.karmawhore-downvotes (str d)]
     [:td.karmawhore-sum (str s)]]))

(defn records-table [records]
  `[:table
    ~@(map record-row records)])

(defn html-wrapper [records]
  [:html
   [:head
    [:title "Karma statistics"]]
   [:body
    [:p
     (records-table records)]
    [:p "Powered by Karmawhore"]]])

(defn records->html [records]
  (html (html-wrapper records)))
