package org.powerlevel

import cats.effect.IO
import cats.implicits.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.*
import doobie.util.ExecutionContexts
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
//import org.powerlevel.Database.{getPlayerTotalGames, getPlayersLatestMatchID}

// import sglicko2.{Leaderboard, Glicko2, Scale}
import sglicko2.*
import sglicko2.WinOrDraw.*
import sglicko2.WinOrDraw.Ops.*
import sglicko2.RatingPeriod
import cats.effect.unsafe.implicits.global


case class ReplayResults(uniqueMatchID: Long, matchTime: Long,
                         winnerID: Long, winnerName: String, winnerCharacters: List[String],
                         loserID: Long, loserName: String, loserCharacters: List[String])

case class DBPlayer(uniquePlayerID: String, name: String, latestMatchTime: String)

case class PlayerGames(matchTime: String,
                       winnerName: String, winnerCharacters: List[String], glickoValueWinner: Double, glickoValueDeviationWinner: Double,
                       loserName: String, loserCharacters: List[String], glickoValueLoser: Double, glickoValueDeviationLoser: Double)

object Database {
//  given Glicko2 = Glicko2(scale = Scale.Glicko)
  given Glicko2 = Glicko2(tau = Tau[0.3d], defaultVolatility = Volatility(0.03d), scale = Scale.Glicko)
  //  given Glicko2 = Glicko2(tau = Tau[1d], defaultVolatility = Volatility(0.1d), scale = Scale.Glicko)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:mytable",
    "docker",
    "docker"
  )

  def saveResult(details: ReplayResults) = {

    val uniqueMatchID = details.uniqueMatchID
    val matchTime = details.matchTime
    val winnerID = details.winnerID
    val winnerName = details.winnerName
    val winnerCharacters = details.winnerCharacters
    val loserID = details.loserID
    val loserName = details.loserName
    val loserCharacters = details.loserCharacters
    val checkMatchID = checkIfMatchIdExists(uniqueMatchID).unsafeRunSync()
    val uniqueMatch: Boolean = if (checkMatchID.length == 0) true else if (checkMatchID.head != uniqueMatchID) true else false

    if ((winnerID != 0 && loserID != 0) && uniqueMatch) {
      val glickoValuesAndDeviation = Main.updateEntireGlickoLeaderboardAfterReplays(winnerID, loserID) // still updating glicko even tho the match might be a duplicate
      // check against match_id sql request to see if it exists and if it is a duplicate then return IO.Unit

      val glicko_value_winner: Double = (glickoValuesAndDeviation.playersByIdInNoParticularOrder(winnerID).confidence95.upper.value + glickoValuesAndDeviation.playersByIdInNoParticularOrder(winnerID).confidence95.lower.value) / 2
      val glicko_deviation_winner: Double = glickoValuesAndDeviation.playersByIdInNoParticularOrder(winnerID).deviation.value

      val glicko_value_loser: Double = (glickoValuesAndDeviation.playersByIdInNoParticularOrder(loserID).confidence95.upper.value + glickoValuesAndDeviation.playersByIdInNoParticularOrder(loserID).confidence95.lower.value) / 2
      val glicko_deviation_loser: Double = glickoValuesAndDeviation.playersByIdInNoParticularOrder(loserID).deviation.value

      val insertGameQuery = (
        sql"""insert into game_results (unique_match_id, match_time, winner_id, winner_name, winner_characters, glicko_value_winner, glicko_deviation_winner, loser_id, loser_name, loser_characters, glicko_value_loser, glicko_deviation_loser) values
           ($uniqueMatchID, $matchTime,
           $winnerID, $winnerName, $winnerCharacters, $glicko_value_winner, $glicko_deviation_winner,
           $loserID, $loserName, $loserCharacters,  $glicko_value_loser, $glicko_deviation_loser)
           on conflict do nothing""".update.run)

//        if(winnerID == 220930094846699492L) {
//          println("under insertGameQuery + " + glicko_value_winner)
//          println(checkMatchID.head)
//          println(uniqueMatchID)
//        }

       val insertWinnerPlayerQuery = (
         sql"""insert into players (unique_player_id, player_name, glicko_value, glicko_deviation) values
            ($winnerID, $winnerName, $glicko_value_winner, $glicko_deviation_winner) on conflict (unique_player_id)
            do update set player_name=excluded.player_name, glicko_value=excluded.glicko_value, glicko_deviation=excluded.glicko_deviation""".update.run)

//         if (winnerID == 220930094846699492L) {
//           println("under insertWinnerPlayerQuery + " + glicko_value_winner)
//         }

       val insertLoserPlayerQuery = (
         sql"""insert into players (unique_player_id, player_name, glicko_value, glicko_deviation) values
            ($loserID, $loserName, $glicko_value_loser, $glicko_deviation_loser) on conflict (unique_player_id)
                    do update set player_name=excluded.player_name, glicko_value=excluded.glicko_value, glicko_deviation=excluded.glicko_deviation""".update.run)

//         if (winnerID == 220930094846699492L) {
//           println("under insertLoserPlayerQuery + " + glicko_value_winner)
//         }

//      Main.updateEntireGlickoLeaderboardAfterReplays(winnerID, loserID)

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

  def getPlayerLastGlicko(user_id: Long): IO[List[(Double, Double)]] = {
    val f1 = fr"select glicko_value, glicko_deviation from players where unique_player_id = $user_id"
    f1.query[(Double, Double)].to[List].transact(xa)
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

  def checkIfMatchIdExists(match_id: Long): IO[List[Long]] = {
    val f1 = fr"select unique_match_id from game_results where unique_match_id=$match_id order by match_time desc limit 1"
    f1.query[Long].to[List].transact(xa)
  }


  def searchPlayer(name: String): IO[List[(String, String, String)]] = {
    val f1 =
      fr"""select distinct on (unique_player_id) unique_player_id, player_name, max(match_time), glicko_value, glicko_deviation from players
          inner join game_results on players.unique_player_id=game_results.winner_id or players.unique_player_id=game_results.loser_id
          where lower(player_name) like $name
          group by unique_player_id;"""

    f1.query[(String, String, String)].to[List].transact(xa)
  }

  def getPlayerGames(user_id: Long) = {
    // TODO case class for mapping
    val f1 = fr"select match_time, winner_name, winner_characters, glicko_value_winner, glicko_deviation_winner, loser_name, loser_characters, glicko_value_loser, glicko_deviation_loser from game_results"
    val f2 = fr"where winner_id = ${user_id} or loser_id = ${user_id}"
    val f3 = fr"order by match_time desc"
    val getGames = (f1 ++ f2 ++ f3).query[(String, String, List[String], Double, Double, String, List[String], Double, Double)]
    getGames.to[List].transact(xa)
  }

//  def findPlayerByName(s: String) // SQL query to get players
// (select * from players where player_name like '%Deviance%';) and map to case class for all their games

  def getPlayerTotalGames(user_id: Long): IO[List[Int]] = {
    val f1 = fr"select count (*) from game_results where winner_id=$user_id or loser_id=$user_id"
    val getTotalGames = (f1).query[Int]
    getTotalGames.to[List].transact(xa)
  }

  def getPlayerWonGames(user_id: Long) = {
    val f1 = fr"select count (*) from game_results where winner_id=$user_id"
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

  def getTop100RankingsInOrder = {
    // TODO case class for mapping
    val f1 = fr"select unique_player_id, player_name, glicko_value, glicko_deviation from players"
    val f2 = fr"order by glicko_value desc limit 100"
    val getGames = (f1 ++ f2).query[(String, String, String, String)]
    getGames.to[List].transact(xa)
  }

  def writeToDB(replayResults:IO[List[ReplayResults]]): IO[List[Any]] = {
    val results = replayResults.flatMap { result =>
      result.traverse { details =>
        saveResult(details)
      }
    }
    // update player column here?? for game numbers and stuff such as number_of_games and wins
    IO.println(Utils.timeNow + ": Finished writing to DB!")
    results
  }

}
