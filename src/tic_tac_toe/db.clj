(ns tic-tac-toe.db
  (:require
   [clojure.string :as str]
   [environ.core :refer [env]]))

(defn- connection []
  (let [[url username password]
        (mapv env [:db-url :db-username :db-password])]
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname url
     :user username
     :password password}))

