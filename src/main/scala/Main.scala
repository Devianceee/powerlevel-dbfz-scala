package org.powerlevel

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import com.comcast.ip4s.*
import io.circe.Encoder.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
//import play.api.libs.json._
import fs2.io.file.Path
import org.http4s.implicits._

import sglicko2.*
import sglicko2.WinOrDraw.*
import sglicko2.WinOrDraw.Ops.*

import math.Numeric.Implicits.infixNumericOps


object Main extends IOApp {

//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  val numberOfMatchesQueried = 100 // better to do this via .conf file or some other environment way
  object playerName extends QueryParamDecoderMatcher[String]("name")

  def getReplaySingle(timestamp:String, fromRank: Int) = Database.writeToDB(Utils.parseReplays(Requests.replayRequest(timestamp, 0, numberOfMatchesQueried, fromRank)))
//  def getUser(name: String) = Database.getUsersWithSimilarName(name) >> IO.println(s"${Thread.currentThread().getName} - Request Finished for User $name! Completed at: ${Utils.timeNow}")
  def replays(timestamp: String, fromRank: Int): IO[Unit] = getReplaySingle(timestamp, fromRank) >> IO.println(s"${Thread.currentThread().getName} - Request Finished for Rank $fromRank! Completed at: ${Utils.timeNow}")


  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

  val routes = HttpRoutes.of[IO] {

    case GET -> Root / "test" =>
      Ok(s"Hello. Time now is ${Utils.timeNow}")

    case request @ GET -> Root =>
      StaticFile.fromPath(Path("frontend/index.html"), Some(request))
        .getOrElseF(NotFound()) // In case the file doesn't exist

    case GET -> Root / "getReplays" => // cron job via curl / python
      val reqTimestamp: String = Requests.getLoginTimeStamp.unsafeRunSync()
      println(reqTimestamp)
      replays(reqTimestamp, 11).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 1001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 1501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 2001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 2501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 3001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 3501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 4001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 4501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 5001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 5501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 6001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 6501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 7001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 7501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 8001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 8501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 9001).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 9501).unsafeRunAsync (_ => ())
      replays(reqTimestamp, 10001).unsafeRunAsync(_ => ())

      // async calls to get replays all in one go in parallel

      Ok(s"Request sent at: ${Utils.timeNow}")

    case GET -> Root / "search" :? playerName(name) =>
      Ok(Utils.searchPlayer(s"%$name%"))

    case GET -> Root / "playerid" / player_id =>
      Ok(Utils.getPlayerGames(player_id.toLong)) // how to parse list -> json in elm?

    // go to elm single page -> sends ping to api backend -> elm parses json response -> displays

    case _ =>
      Ok("Error")

  }.orNotFound

  val server = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"7000")
    .withHttpApp(routes)
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO: run server which can be pinged to be able to cancel scheduled task and gracefully close database in case of maintenance
    // TODO: can probably add more flatMaps to places for better comprehension / less nesting (removes inner IO)
    // TODO: traverse keyword is very nice, see if I can use it in other places
//    println(Utils.parseReplays(Requests.replayRequest(Requests.getLoginTimeStamp.unsafeRunSync(), 0, numberOfMatchesQueried, 11)).unsafeRunSync())
//    server

    given Glicko2 = Glicko2(scale = Scale.Glicko)

//    val updatedLeaderboard = leaderboard after RatingPeriod(1111L winsVs 2222L, 1111L winsVs 2222L, 2222L winsVs 3333L)
//    println((updatedLeaderboard.playersByIdInNoParticularOrder(1111L).confidence95.upper.value + updatedLeaderboard.playersByIdInNoParticularOrder(1111L).confidence95.lower.value) / 2)
//    println(updatedLeaderboard.playersByIdInNoParticularOrder(1111L).deviation.value)
//    println(updatedLeaderboard.playersInRankOrder)
//    println()
//    val foo = Utils.glickoUpdateGames(1111L, 2222L, updatedLeaderboard)
//
//    println((foo.playersByIdInNoParticularOrder(1111L).confidence95.upper.value + foo.playersByIdInNoParticularOrder(1111L).confidence95.lower.value) / 2)
//    println(foo.playersByIdInNoParticularOrder(1111L).deviation.value)
//    println(foo.playersInRankOrder)
//    println(Database.getAllPlayersLastGlicko.unsafeRunSync())

    val leaderboard = Utils.updateEntireGlickoLeaderboard

    // general workflow for glicko ^
    IO(ExitCode.Success)
  }
}
