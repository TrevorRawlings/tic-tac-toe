(ns tic-tac-toe.core
  (:require
   [tic-tac-toe.http-server :as http])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; TODO parse the command line args to determine the port
  (http/start {:http-port 3000}))
