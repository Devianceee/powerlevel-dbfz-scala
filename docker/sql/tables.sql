-- Database
CREATE DATABASE mytable;
\c mytable;

-- Matches
CREATE TABLE replay_results (
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
