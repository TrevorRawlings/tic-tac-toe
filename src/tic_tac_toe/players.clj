(ns tic-tac-toe.players
  (:require
   [tic-tac-toe.db :as db]))

(def select-players
  {:select [:p.id, :p.name, :p.created-at, :p.updated-at]
   :from   [[:players :p]]
   :order-by [:p.name]})

(defn all-players
  "returns all of the players"
  []
  (db/query select-players))

(defn load-player-by-id [player-id]
  (-> (assoc select-players :where [:= :id player-id])
      db/query
      first))
