(ns tic-tac-toe.spec
  "Specs that define the dependant types (as relaxed as possible) and entity maps "
  (:require
   [clj-uuid :as uuid]
   [clojure.spec.alpha :as s]))

(s/def ::uuid uuid?)
(s/def ::uuid-string (s/and string? uuid/uuid-string?))
(s/def ::uuid-str-or-latest (s/or ::uuid-string #{"latest"}))

(s/def ::player-id uuid?)
(s/def ::player (s/keys :req-un [::id
                                 ::name]))

(s/def ::row (s/int-in 0 3))
(s/def ::column (s/int-in 0 3))
(s/def ::move (s/keys :req-un [::player-id
                               ::row
                               ::column]))
(s/def ::moves  (s/coll-of ::move))
(s/def ::game (s/keys :req-un [::id ::state]))

(s/def ::http-port pos-int?)
(s/def ::config (s/keys :req-un [::http-port]))

