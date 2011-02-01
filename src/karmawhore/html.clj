(ns karmawhore.html
  (:use [hiccup.core :only (html)]))

(defn record-row [record]
  (let [[nick {u :upvotes d :downvotes s :sum}] record]
    [:tr
     [:td.nick nick]
     [:td.upvotes (str u)]
     [:td.downvotes (str d)]
     [:td.sum (str s)]]))

(defn records-table [records]
  `[:table
    [:tr
     [:th "Nick"]
     [:th "Upvotes"]
     [:th "Downvotes"]
     [:th "Sum"]]
    ~@(map record-row records)])

(defn html-wrapper [records]
  [:html
   [:head
    [:title "Karma statistics"]
    [:link {:rel "stylesheet" :type "text/css" :href "karmawhore.css"}]]
   [:body
    [:h2 "Users sorted by Karma"]
    [:p
     (records-table records)]
    [:p#powered-by
     "Powered by "
     [:a {:href "https://github.com/Leonidas-from-XIV/karmawhore"} "Karmawhore"]]]])

(defn records->html [records]
  (html (html-wrapper records)))
