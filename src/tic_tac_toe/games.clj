(ns tic-tac-toe.games
  (:require
   [clj-postgresql.types]
   [clojure.java.jdbc :as jdbc]
   [clojure.spec.alpha :as s]
   [inflections.core :as infl]
   [tic-tac-toe.db :as db]
   [tic-tac-toe.spec :as spec]
   [tic-tac-toe.games.tic-tac-toe :as ttt]))

(s/fdef all-games-for-player
  :args (s/cat :player ::spec/player)
  :ret (s/coll-of ::spec/game))

(defn all-games-for-player
  "returns all of the games associated with the user. Returns games ordered by created-at,
  with the most recent game first"
  [player-id]
  (db/query {:select [:g.id, :g.state, :g.created-at, :g.updated-at]
             :from   [[:games :g]]
             :join [[:game-players :gp] [:= :gp.game_id :g.id]]
             :where  [:= :gp.player-id (:id player-id)]
             :order-by [[:g.created-at :desc]]}))

(defn find-game-by-id
  "Returns the game for the specified player or nil if the record does not exist"
  [player game-id-or-string]
  (let [filter-fn (if (= "latest" game-id-or-string)
                    (constantly true)
                    #(= (str (:id %)) game-id-or-string))]
    (some->> player
             all-games-for-player
             (filter filter-fn)
             first)))

(s/fdef save
  :args (s/cat :game ::spec/game)
  :ret ::spec/game)

(defn save [{:keys [:id :state] :as game}]
  (let [updated-at (db/now)
        game* {:state state :updated_at updated-at}]
    (db/update! :games game* ["id = ?" id])
    (assoc game :updated-at updated-at)))

(s/fdef create
  :args (s/cat :player-1 ::spec/player
               :player-2 ::spec/player)
  :ret ::spec/game)

(defn create
  "Saves a new game with players player-1 & player-2. Returns the newly created game"
  [player-1 player-2]
  (jdbc/with-db-transaction [conn (db/connection)]
    (let [state (ttt/new-game player-1 player-2)
          game (-> (jdbc/insert! conn :games {:state state})
                   first
                   infl/hyphenate-keys)]
      (jdbc/insert! conn :game_players {:game_id (:id game) :player_id (:id player-1)})
      (jdbc/insert! conn :game_players {:game_id (:id game) :player_id (:id player-2)})
      game)))

(defn validate-next-move
  [{:keys [state] :as game} move]
  (ttt/validate-movement state move))

(defn play-next-move
  [{:keys [state] :as game} _ move]
  (let [game* (assoc game :state (ttt/play-next-move state move))]
    (save game*)))

;; TODO: This should probably be in a separate namespace.
(defn render [game]
  (ttt/render (:state game)))
