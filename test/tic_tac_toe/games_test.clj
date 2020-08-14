(ns tic-tac-toe.games-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [tic-tac-toe.games :as g]
   [tic-tac-toe.spec :as spec]
   [tic-tac-toe.test-helpers :as helpers]))

(defn player-part-of-game?
  "returns true if the player is associated with the supplied game"
  [player game]
  (let [games (set (map :id (g/all-games-for-player player)))]
    (boolean (games (:id game)))))

(deftest create-test
  (testing "It creates a new game and assigns the two players"
    (let [[player-1 player-2 game] (helpers/create-game-and-players)]
      (is (s/valid? ::spec/game game) "The returned object should be a valid game")
      (doseq [player [player-1 player-2]]
        (is (player-part-of-game? player game))))))

(deftest save-test
  (testing "Changes are persisted to the database"
    (let [[_ _ game] (helpers/create-game-and-players)
          game* (assoc game :state {:items [1 2 3]})]

      (g/save game*)
      (let [reloaded-record (helpers/load-game (:id game))]
        (is (= (:state reloaded-record) {:items [1 2 3]}))))))

(deftest all-games-for-player-test
  (let [[player-1 player-2 game] (helpers/create-game-and-players)]
    (testing "returns games linked to the specified player"
      (let [games (g/all-games-for-player player-1)]
        (is (= 1 (count games)) "The newly create player should have a single game")
        (is (player-part-of-game? player-1 game))
        (is (player-part-of-game? player-2 game)))

      (testing "does not return games that belong to other players"
        (let [[_ _ other-game] (helpers/create-game-and-players)]
          (is (not (player-part-of-game? player-1 other-game)))
          (is (not (player-part-of-game? player-2 other-game))))))))
