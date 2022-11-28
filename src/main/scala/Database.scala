package org.powerlevel

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor._
import doobie.util.ExecutionContexts

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

case class ReplayResults(uniqueMatchID: Long, matchTime: Long,
                         winnerID: Long, winnerName: String, winnerCharacters: List[String],
                         loserID: Long, loserName: String, loserCharacters: List[String])

case class Player(uniquePlayerID: String, name: String, latestMatchTime: String)

case class PlayerGames(matchTime: String,
                       winnerName: String, winnerCharacters: List[String],
                       loserName: String, loserCharacters: List[String])

object Database {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:mytable",
    "docker",
    "docker"
  )

  def saveResult(details: ReplayResults) = {

//    println(details)
    val uniqueMatchID = details.uniqueMatchID
    val matchTime = details.matchTime
    val winnerID = details.winnerID
    val winnerName = details.winnerName
    val winnerCharacters = details.winnerCharacters
    val loserID = details.loserID
    val loserName = details.loserName
    val loserCharacters = details.loserCharacters

    if (winnerID != 0 && loserID != 0) {
      val insertGameQuery =
        sql"""insert into game_results (unique_match_id, match_time,
           winner_id, winner_name, winner_characters,
           loser_id, loser_name, loser_characters) values
           ($uniqueMatchID, $matchTime,
           $winnerID, $winnerName, $winnerCharacters,
           $loserID, $loserName, $loserCharacters) on conflict do nothing""".update.run

      val insertWinnerPlayerQuery =
        sql"""insert into players (unique_player_id, player_name) values
           ($winnerID, $winnerName) on conflict do nothing""".update.run

      val insertLoserPlayerQuery =
        sql"""insert into players (unique_player_id, player_name) values
           ($loserID, $loserName) on conflict do nothing""".update.run

      val run = for {
        run1 <- insertGameQuery
        run2 <- insertWinnerPlayerQuery
        run3 <- insertLoserPlayerQuery
      } yield (run1, run2, run3)

      run.transact(xa)
    }
    else {
      IO.unit
    }
  }

  def searchPlayer(name: String): IO[List[(String, String, String)]] = {
//    val f1 = fr"select unique_player_id, player_name from players"
//    val f2 = fr"where lower(player_name)"
//    val f3 = fr"like $name"
    val f1 =
      fr"""select distinct on (unique_player_id) unique_player_id, player_name, max(match_time) from players
          inner join game_results on players.unique_player_id=game_results.winner_id or players.unique_player_id=game_results.loser_id
          where lower(player_name) like $name
          group by unique_player_id;"""

    f1.query[(String, String, String)].to[List].transact(xa)
  }

  def getPlayerGames(user_id: Long) = {
    // TODO case class for mapping
    val f1 = fr"select match_time, winner_name, winner_characters, loser_name, loser_characters from game_results"
    val f2 = fr"where winner_id = ${user_id} or loser_id = ${user_id}"
    val f3 = fr"order by match_time desc"

    val getGames = (f1 ++ f2 ++ f3).query[(String, String, List[String], String, List[String])]
    getGames.to[List].transact(xa)
  }

//  def findPlayerByName(s: String) // SQL query to get players
// (select * from players where player_name like '%Deviance%';) and map to case class for all their games


  def writeToDB(replayResults:IO[List[ReplayResults]]): IO[List[Any]] = {
    val results = replayResults.flatMap { result =>
      result.traverse { details =>
        saveResult(details)
      }
    }
    IO.println(Utils.timeNow + ": Finished writing to DB!")
    results
  }

}
