-- Database
CREATE DATABASE mytable;
\c mytable;

-- Matches
-- select pg_size_pretty(pg_relation_size('game_results')); for table size
-- select * from game_results; for all games
-- select * from game_results where winner_id = 'INSERT_PLAYER_ID' or loser_id = 'INSERT_PLAYER_ID'; for all games from specific player
-- select * from game_results where winner_id = 'INSERT_PLAYER_ID' or loser_id = 'INSERT_PLAYER_ID' order by match_time desc; for all games from specific player ordered by date descending

CREATE TABLE game_results (
  unique_match_id bigint NOT NULL UNIQUE,
  match_time character varying NOT NULL,
  winner_id bigint NOT NULL,
  winner_name character varying NOT NULL,
  winner_characters VARCHAR[] NOT NULL,
  loser_id bigint NOT NULL,
  loser_name character varying NOT NULL,
  loser_characters VARCHAR[] NOT NULL,
  PRIMARY KEY (unique_match_id)
);

-- Players
-- select pg_size_pretty(pg_relation_size('players')); for table size
-- select * from players; for all players
-- -- select * from players where lower(player_name) like '%INSERT_PLAYER_NAME%'; for finding players with similar name
-- -- select * from players where unique_player_id = 'INSERT_UNIQUE_PLAYER_ID'; for finding specific player after finding ID

CREATE TABLE players (
  unique_player_id bigint NOT NULL UNIQUE,
  player_name character varying NOT NULL,
  PRIMARY KEY (unique_player_id)
);

-- https://stackoverflow.com/questions/24718706/backup-restore-a-dockerized-postgresql-database