(ns tic-tac-toe.games.tic-tac-toe-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [clj-uuid :as uuid]
   [tic-tac-toe.games.tic-tac-toe :as ttt]
   [tic-tac-toe.spec :as spec]
   [tic-tac-toe.test-helpers :as helpers]))

(deftest render-test
  (let [player-1 {:id (uuid/v4)}
        player-2 {:id (uuid/v4)}
        new-game (ttt/new-game player-1 player-2)
        result (ttt/render new-game)
        positions [[0 0] [0 1] [0 2]
                   [1 0] [1 1] [1 2]
                   [2 0] [2 1] [2 2]]]
    (is (= result {:board [[".",".","."],
                           [".",".","."],
                           [".",".","."]],
                   :message ""}))

    (testing "0 has won"
      (let [players (flatten (repeat [player-1 player-2]))
            moves (map (fn [player [row column]]
                         {:player-id (:id player)
                          :row row
                          :column column})
                       players positions)
            game (assoc new-game :moves moves)
            result (ttt/render game)]
        (is (= result {:board [["0","X","0"],
                               ["X","0","X"],
                               ["0","X","0"]],
                       :message "0 has won!"}))))

    (testing "X has won"
      (let [players (flatten (repeat [player-2 player-1]))
            moves (map (fn [player [row column]]
                         {:player-id (:id player)
                          :row row
                          :column column})
                       players positions)
            game (assoc new-game :moves moves)
            result (ttt/render game)]
        (is (= result {:board [["X","0","X"],
                               ["0","X","0"],
                               ["X","0","X"]],
                       :message "X has won!"}))))))

(deftest draw-test
  (is (true? (ttt/draw? [["0","X","0"],
                         ["0","X","0"],
                         ["X","0","X"]])))

  (is (false? (ttt/draw? [[".","X","0"],
                          ["0","X","0"],
                          ["X","0","X"]]))))

  ;
  ;    result (ttt/render new-game)]
  ;(is (= result {:board [[".",".","."],
  ;                       [".",".","."],
  ;                       [".",".","."]],
  ;               :message ""}))))