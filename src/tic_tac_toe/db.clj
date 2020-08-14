(ns tic-tac-toe.db
  (:require
   [clj-postgresql.core]                                   ;; Adds extentions for parsing JSONB columns
   [clj-postgresql.types]
   [clojure.java.jdbc :as jdbc]
   [clojure.walk :as walk]
   [environ.core :refer [env]]
   [honeysql.core :as sql]
   [inflections.core :as infl])
  (:import (java.sql Timestamp)))

(defn connection []
  (let [[name username password host port]
        (mapv env [:db-name :db-username :db-password :db-host :db-port])]
    {:classname "org.postgresql.Driver"
     :subprotocol  "postgresql"
     :subname (format "//%s:%s/%s" host port name)
     :user username
     :password password}))

(defn query [sql-map]
  (->> (sql/format sql-map)
       (jdbc/query (connection))
       (mapv infl/hyphenate-keys)
       (mapv walk/keywordize-keys)))

(def insert! (partial jdbc/insert! (connection)))
(def update! (partial jdbc/update! (connection)))

(defn now []
  (new Timestamp (System/currentTimeMillis)))
