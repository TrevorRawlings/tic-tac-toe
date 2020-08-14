(ns tic-tac-toe.http-server
  (:require
   [muuntaja.core :as m]
   [clj-uuid :as uuid]
   [clojure.core.match :as cm]
   [clojure.spec.alpha :as s]
   [reitit.coercion.spec]
   [reitit.dev.pretty :as pretty]
   [reitit.http :as http]
   [reitit.http.coercion :as coercion]
   [reitit.http.interceptors.exception :as exception]
   [reitit.http.interceptors.muuntaja :as muuntaja]
   [reitit.http.interceptors.parameters :as parameters]
   [reitit.interceptor.sieppari :as sieppari]
   [reitit.ring :as ring]
   [reitit.spec :as rs]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cookies :as rcookies]
   [tic-tac-toe.games :as games]
   [tic-tac-toe.players :as players]
   [tic-tac-toe.spec :as spec]))

;; TODO: Replace with user authentication. The session cookie should be signed when a secret that is validated
;;       before data in the cookie is used

(defn ->uuid [uuid-str]
  (when (uuid/uuid-string? uuid-str)
    (uuid/as-uuid uuid-str)))

(defn player-id-from-cookie
  "Work around for the 'wrap-load-user' middleware function below not getting called"
  [request]
  (some->>
   (get-in (rcookies/cookies-request  request) [:cookies "player-id" :value])
   ->uuid
   players/load-player-by-id))

(defn wrap-load-user
  ([handler]
   (wrap-load-user handler {}))
  ([handler _]
   (fn
     ([request]
      (handler (let [->uuid #(when (uuid/uuid-string? %)
                               (uuid/as-uuid %))
                     player (some->>
                             (get-in request [:cookies "player-id" :value])
                             ->uuid
                             players/load-player-by-id)]
                 (assoc request :player player)))))))

(defn get-players [_]
  {:status 200
   :body (players/all-players)})

(s/def ::game-id ::spec/uuid-str-or-latest)
(s/def ::new-game-params (s/keys :req-un []))
(s/def ::get-game-params (s/keys :req-un [::game-id]))
(s/def ::post-game-move-path-params (s/keys :req-un [::game-id]))
(s/def ::post-game-move-query-params (s/keys :req-un [::spec/row
                                                      ::spec/column]))

(defn post-game-new [{{:strs [other-player-id]} :params :as request}]
  (let [player (player-id-from-cookie request)
        other-player (some->> other-player-id
                              ->uuid
                              players/load-player-by-id)
        new-game (when (and player other-player)
                   (games/create player other-player))]

    (cm/match
     [(boolean player) (boolean other-player)]
      [false _] {:status 401 :body "Session invalid"}
      [true false] {:status 404 :body "Not Found"}
      [true true] {:status 201 :body (games/render new-game)})))

(defn get-game [{{:keys [game-id]} :path-params :as request}]
  (let [player (player-id-from-cookie request)
        game (when player (games/find-game-by-id player game-id))]

    (cm/match
     [(boolean player) (boolean game)]
      [false _] {:status 401 :body "Session invalid"}
      [true false] {:status 404 :body "Not Found"}
      [true true] {:status 200 :body (games/render game)})))

(defn post-game-move [{{{:keys [game-id]} :path {:keys [row column]} :query} :parameters :as request}]
  (let [player (player-id-from-cookie request)
        game (when player (games/find-game-by-id player game-id))
        move {:row row :column column :player-id (:id player)} ;; TODO row & column is specific to tic-tac-toe ... somehow make this generic
        validation (when game (games/validate-next-move game move))
        game* (when (:success validation) (games/play-next-move game player move))]

    (cm/match
     [(boolean player) (boolean game) (:success validation)]
      [false _ _] {:status 401 :body "Session invalid"}
      [true false _] {:status 404 :body "Not Found"}
      [true true false] {:status 409 :body (:message validation)}
      [true true true] {:status 201 :body (games/render game*)})))

(def app
  (http/ring-handler
   (http/router
    [["/swagger.json"
      {:get {:no-doc true
             :swagger {:info {:title "tic-tac-toe--api"
                              :description "HTTP interface for creating and playing tic-tac-toe games"}}
             :handler (swagger/create-swagger-handler)}}]
     ["/api"
      {:middleware [[rcookies/wrap-cookies] ;; FIXME for some reason this is being ignored
                    [wrap-load-user]]}
      ["/players" {:get {:summary "Returns all of the available players"
                         :responses {200 {:body any?}}
                         :handler get-players}}]
      ["/games"
       ["/new" {:post {:responses {201 {:body map?}
                                   404 {:body string?}
                                   409 {:body string?}}
                       :summary "Create a new game for the specified players"
                       :coercion reitit.coercion.spec/coercion
                       :parameters {:path ::new-game-params}
                       :handler post-game-new}}]
       ["/:game-id"
        ["/" {:get {:responses {200 {:body map?}
                                404 {:body string?}
                                401 {:body string?}}
                    :summary "fetches details of the specified game. Pass 'latest' to return details of the most recently created game"
                    :coercion reitit.coercion.spec/coercion
                    :parameters {:path ::get-game-params}
                    :handler get-game}}]
        ["/move" {:post {:responses {201 {:body map?}
                                     404 {:body string?}
                                     409 {:body string?}}
                         :summary "Append next move to the game"
                         :coercion reitit.coercion.spec/coercion
                         :parameters {:path ::post-game-move-path-params
                                      :query ::post-game-move-query-params}
                         :handler post-game-move}}]]]]]
    {:validate rs/validate
     :exception pretty/exception
     :data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :interceptors [;; swagger feature
                           swagger/swagger-feature
                           rcookies/wrap-cookies
                             ;; query-params & form-params
                           (parameters/parameters-interceptor)
                             ;; content-negotiation
                           (muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                           (muuntaja/format-response-interceptor)
                             ;; exception handling
                           (exception/exception-interceptor)
                             ;; decoding request body
                           (muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                           (coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                           (coercion/coerce-request-interceptor)]}})

   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-default-handler))
   {:executor sieppari/executor
    :middleware [[rcookies/wrap-cookies]
                 [wrap-load-user]]}))

(s/fdef start
  :args (s/cat :config ::spec/config))

(defn start
  "Starts a HTTP server listening on the specified port"
  [config]
  (jetty/run-jetty #'app {:port (:http-port config) :join? false :async false})
  (println "Listening on port" (:http-port config)))
