package org.powerlevel

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor._
import doobie.util.ExecutionContexts

case class ReplayResults(uniqueMatchID: Long, matchTime: Long,
                         winnerID: Long, winnerName: String, winnerCharacters: List[String],
                         loserID: Long, loserName: String, loserCharacters: List[String])
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

//  def findPlayerByName(s: String) // SQL query to get players
//                                  // (select * from players where player_name like '%Deviance%';) and map to case class for all their games


  def writeToDB(replayResults: IO[List[ReplayResults]]) = {

    replayResults.flatMap { result =>
      result.traverse { details =>
        saveResult(details)
      }
    }

  }
}
