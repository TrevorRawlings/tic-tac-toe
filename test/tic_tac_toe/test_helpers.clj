(ns tic-tac-toe.test-helpers
  (:require
   [clojure.test :refer :all]
   [clj-uuid :as uuid]
   [tic-tac-toe.games :as g]
   [tic-tac-toe.db :as db]))

(defn create-player
  "Inserts a new player record into the database and returns a map containing the record"
  []
  (let [unique-name (str "test-" (uuid/v4))]
    (db/insert! :players {:name unique-name})
    (first
     (db/query {:select [:p.*]
                :from [[:players :p]]
                :where [:= :p.name unique-name]}))))

(defn create-game-and-players []
  (let [player-1 (create-player)
        player-2 (create-player)
        game (g/create player-1 player-2)]
    [player-1 player-2 game]))

(defn load-game
  "Fetches the specified game from the database"
  [id]
  (first
   (db/query {:select [:g.*]
              :from [[:games :g]]
              :where [:= :g.id id]})))
