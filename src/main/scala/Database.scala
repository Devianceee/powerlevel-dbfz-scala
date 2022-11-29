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
      val glickoValuesAndDeviation = Utils.glickoUpdateGames(winnerID, loserID, ???)
      /* take latest value and deviation from last played game from player table
      *  take whoever won and lost and return value

      *  with the returned result, update each players value and deviation and insert into "val insertGameQuery" the values and deviation for both players
         and returns back to main with the updated leaderboard
      * */

      val insertGameQuery =
        sql"""insert into game_results (unique_match_id, match_time,
           winner_id, winner_name, winner_characters,
           loser_id, loser_name, loser_characters) values
           ($uniqueMatchID, $matchTime,
           $winnerID, $winnerName, $winnerCharacters,
           $loserID, $loserName, $loserCharacters) on conflict do nothing""".update.run

      val insertWinnerPlayerQuery =
        sql"""insert into players (unique_player_id, player_name) values
           ($winnerID, $winnerName) on conflict do update set player_name = excluded.player_name""".update.run

      val insertLoserPlayerQuery =
        sql"""insert into players (unique_player_id, player_name) values
           ($loserID, $loserName) on conflict do update set player_name = excluded.player_name""".update.run

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

  def getPlayerLastGlicko(user_id: Long): IO[List[(Long, Double, Double)]] = {
    val f1 = fr"select unique_player_id, glicko_value, glicko_deviation from players where unique_player_id = $user_id"
    f1.query[(Long, Double, Double)].to[List].transact(xa)
  }

  def getAllPlayersLastGlicko: IO[List[(Long, Double, Double)]] = {
    val f1 = fr"select unique_player_id, glicko_value, glicko_deviation from players"
//    val f1 = fr"select unique_player_id from players"
    f1.query[(Long, Double, Double)].to[List].transact(xa)
  }

  def getAllPlayersIDs: IO[List[Long]] = {
    val f1 = fr"select unique_player_id from players"
    f1.query[Long].to[List].transact(xa)
  }


  def searchPlayer(name: String): IO[List[(String, String, String)]] = {
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
    // add query to COUNT() all their games to get total games
    val getGames = (f1 ++ f2 ++ f3).query[(String, String, List[String], String, List[String])]
    getGames.to[List].transact(xa)
  }

//  def findPlayerByName(s: String) // SQL query to get players
// (select * from players where player_name like '%Deviance%';) and map to case class for all their games

  def getPlayerTotalGames(user_id: Long) = {
    val f1 = fr"select count * from game_results where winner_id=$user_id or loser_id=$user_id"
    val getTotalGames = (f1).query[Int]
    getTotalGames.to[List].transact(xa)
  }

  def getPlayerWonGames(user_id: Long) = {
    val f1 = fr"select count * from game_results where winner_id=$user_id"
    val getTotalGames = (f1).query[Int]
    getTotalGames.to[List].transact(xa)
  }

  def getAllRankingsInOrder = {
    // TODO case class for mapping
    val f1 = fr"select unique_player_id, player_name, glicko_value, glicko_deviation from players"
    val f2 = fr"order by glicko_value desc"
    val getGames = (f1 ++ f2).query[(String, String, String, String)]
    getGames.to[List].transact(xa)
  }

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
