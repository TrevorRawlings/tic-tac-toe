(ns tic-tac-toe.games.tic-tac-toe
  (:require
   [clj-postgresql.types]))

(defn- empty-board []
  [["." "." "."]
   ["." "." "."]
   ["." "." "."]])

(defn- id->keyword [uuid]
  (-> uuid str keyword))

(defn- construct-board
  "Applies each of the moves one at a time to rebuild the current state of the game"
  [{:keys [moves players]}]
  (let [apply-move (fn [board {:keys [player-id row column]}]
                     (let [character (players (id->keyword player-id))]
                       (assoc-in board [row column] character)))]
    (reduce apply-move (empty-board) moves)))

(defn- player-has-won?
  "Returns true if the player with 'piece' has won"
  [board piece]
  (let [selectors [first second last]
        win? (fn [items] (every? #(= % piece) items))]
    (some true?
          (flatten
           [;; check for complete rows
            (map (fn [row] (win? (row board))) selectors)
            ;; check for complete columns
            (map (fn [column] (win? (map column board))) selectors)
            ;; check the diagonals
            (win? (map (fn [i] (get-in board [i i])) [0 1 2]))
            (win? (map (fn [i] (get-in board [i (- 2 i)])) [0 1 2]))]))))

(defn winner
  "Returns 'O' or 'X' if the player has won"
  [board]
  (->> ["0" "X"]
       (filter (partial player-has-won? board))
       first))

(defn draw?
  "Returns true and all of the possible moves have been made"
  [board]
  (->> (flatten board)
       (remove #(= "." %))
       count
       (= 9)))

(defn validate-movement
  [{:keys [players moves] :as game-state} {:keys [row column] :as move}]

  (let [board (construct-board game-state)
        player-id (id->keyword (:player-id move))
        invalid-player (not (contains? players player-id))
        game-over (or (boolean (winner board))
                      (draw? board))
        already-taken (not= (get-in board [row column])
                            ".")
        last-player (-> (last moves) :player-id id->keyword)
        wrong-player (= last-player player-id)]
    (cond
      invalid-player {:success false :message "Invalid Player"}
      game-over {:success false :message "Game has already been completed"}
      already-taken {:success false :message "Position on the board has already been taken"}
      wrong-player {:success false :message "Wrong player"}
      :else {:success true})))

(defn play-next-move
  [game-state move]
  (update game-state :moves conj move))

(defn render
  [game]
  (let [board (construct-board game)
        winner (winner board)
        draw (draw? board)
        message (cond
                  winner (str winner " has won!")
                  draw "It is a draw!"
                  :else "")]
    {:board board
     :message message}))

(defn new-game
  [player-1 player-2]
  {:players {(id->keyword (:id player-1)) "0"
             (id->keyword (:id player-2)) "X"}
   :moves []})
