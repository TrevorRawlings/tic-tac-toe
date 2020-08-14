CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE players (
   id uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v1(),
   name VARCHAR(255) NOT NULL,
   created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
   updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE games (
   id uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v1(),
   state JSONB NOT NULL,
   created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
   updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE game_players (
 id uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v1(),
 game_id uuid NOT NULL REFERENCES games(id),
 player_id uuid NOT NULL REFERENCES players(id),
 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
 UNIQUE (game_id, player_id)
)
