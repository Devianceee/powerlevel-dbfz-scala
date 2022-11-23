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

  def saveResult(details: ReplayResults): IO[Int] = {

    println(details)
    val uniqueMatchID = details.uniqueMatchID
    val matchTime = details.matchTime
    val winnerID = details.winnerID
    val winnerName = details.winnerName
    val winnerCharacters = details.winnerCharacters
    val loserID = details.loserID
    val loserName = details.loserName
    val loserCharacters = details.loserCharacters
    println(winnerCharacters)
    println(loserCharacters)

    val query =
      sql"insert into replay_results (unique_match_id, match_time, winner_id, winner_name, winner_characters, loser_id, loser_name, loser_characters) values ($uniqueMatchID, $matchTime, $winnerID, $winnerName, $winnerCharacters, $loserID, $loserName, $loserCharacters)"
    query.update.run.transact(xa)
  }

  def writeToDB(replayResults: IO[List[ReplayResults]]) = {

    replayResults.flatMap { result =>
      result.traverse { details =>
        saveResult(details)
      }
    }
  }
}
