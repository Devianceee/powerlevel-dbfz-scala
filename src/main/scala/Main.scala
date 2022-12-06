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

//import sglicko2.*
//import sglicko2.WinOrDraw.*
//import sglicko2.WinOrDraw.Ops.*

import math.Numeric.Implicits.infixNumericOps


object Main extends IOApp {
//  given Glicko2 = Glicko2(tau = Tau[0.3d], defaultVolatility = Volatility(0.03d), scale = Scale.Glicko)

//  def updateEntireGlickoLeaderboardAfterReplays(winnerID: Long, loserID: Long): Leaderboard[Long] = {
//    leaderboard = leaderboard.after(RatingPeriod(winnerID winsVs loserID))
//    return leaderboard
//  }

  val numberOfMatchesQueried = 50 // better to do this via .conf file or some other environment way

  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")
  println("Preparing Glicko leaderboard...")
//  var leaderboard = Utils.bootUpdateEntireGlickoLeaderboard
  println("Finished preparing Glicko leaderboard!")

  object playerName extends QueryParamDecoderMatcher[String]("name")

  def getReplaySingle(timestamp:String, fromRank: Int) = Database.writeToDB(Utils.parseReplays(Requests.replayRequest(timestamp, 0, numberOfMatchesQueried, fromRank)))
  def replays(timestamp: String, fromRank: Int): IO[Unit] = getReplaySingle(timestamp, fromRank) >> IO.println(s"${Thread.currentThread().getName} - Request Finished for Rank $fromRank! Completed at: ${Utils.timeNow}")


  val routes = HttpRoutes.of[IO] {

    case GET -> Root / "test" =>
      Ok(s"Hello. Time now is ${Utils.timeNow}")

    case request @ GET -> Root / "search" :? name =>
      StaticFile.fromPath(Path("frontend/search.html"), Some(request))
        .getOrElseF(NotFound())

    case request@GET -> Root / "player" / name =>
      StaticFile.fromPath(Path("frontend/player.html"), Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root =>
      StaticFile.fromPath(Path("frontend/index.html"), Some(request))
        .getOrElseF(NotFound())

    case request@GET -> Root / "stats" =>
      StaticFile.fromPath(Path("frontend/stats.html"), Some(request))
        .getOrElseF(NotFound())

    case request@GET -> Root / "faq" =>
      StaticFile.fromPath(Path("frontend/faq.html"), Some(request))
        .getOrElseF(NotFound())

    case request@GET -> Root / "vip" =>
      StaticFile.fromPath(Path("frontend/vip.html"), Some(request))
        .getOrElseF(NotFound())

    case GET -> Root / "api" / "getReplays" => // cron job via curl / python
      val reqTimestamp: String = Requests.getLoginTimeStamp.unsafeRunSync()
      println(reqTimestamp)
      replays(reqTimestamp, 1).unsafeRunSync()
      replays(reqTimestamp, 1001).unsafeRunSync()
      replays(reqTimestamp, 2001).unsafeRunSync()
      replays(reqTimestamp, 3001).unsafeRunSync()
      replays(reqTimestamp, 4001).unsafeRunSync()
      replays(reqTimestamp, 5001).unsafeRunSync()
      replays(reqTimestamp, 6001).unsafeRunSync()
      replays(reqTimestamp, 7001).unsafeRunSync()
      replays(reqTimestamp, 8001).unsafeRunSync()
      replays(reqTimestamp, 9001).unsafeRunSync()
      replays(reqTimestamp, 10001).unsafeRunSync()

//      val foo = (replays(reqTimestamp, 11), replays(reqTimestamp, 501), replays(reqTimestamp, 1001), replays(reqTimestamp, 1501), replays(reqTimestamp, 2001), replays(reqTimestamp, 2501),
//        replays(reqTimestamp, 3001), replays(reqTimestamp, 3501), replays(reqTimestamp, 4001), replays(reqTimestamp, 4501), replays(reqTimestamp, 5001)).parMapN { (_, _, _, _, _, _, _, _, _, _, _) => () }
//      foo.unsafeRunSync()

      Ok(s"Request sent at: ${Utils.timeNow}")

    case GET -> Root / "api" / "search" :? playerName(name) =>
      val lowercase = name.toLowerCase()
      Ok(Utils.searchPlayer(s"%$lowercase%"))

    case GET -> Root / "api" / "playerid" / player_id =>
      Ok(Utils.getPlayerGames(player_id.toLong))

    case GET -> Root / "api" / "getStats" =>
      Ok(Utils.getStats)

    case GET -> Root / "api" / "top100" =>
      Ok(Utils.getTop100RankingsInOrder)

    case _ =>
      Ok("Error")

  }.orNotFound

  val server = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"9000")
    .withHttpApp(routes)
    .build
    .use(_ => IO.never)
    .as(ExitCode.Success)

  override def run(args: List[String]): IO[ExitCode] = {
      server
//    IO(ExitCode.Success)
  }
}
