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


import math.Numeric.Implicits.infixNumericOps


object Main extends IOApp {

  val numberOfMatchesQueried = 50 // better to do this via .conf file or some other environment way

  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

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

    case GET -> Root / "api" / "deviationDecay" =>
      Utils.deviationDecay.unsafeRunSync()
      Ok(s"Request sent at: ${Utils.timeNow}")

    case GET -> Root / "api" / "getReplays" =>
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
  }
}
