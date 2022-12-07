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

    case GET -> Root / "api" / "deviationDecay" => // cron job via curl / python
      Ok("a")

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
//    println("Player with 1600 value and 200 deviation WINS VERSUS 1700 value and 300 deviation")
//    println("\nNew values")
//    println(GlickoRater.calcNewRating(Rating(1701.0, 136.0), Rating(1506.0, 131.0), 1.0) + " value increase for player 1 winning") // get new rating value
//    println(GlickoRater.calcNewRating(Rating(1506.0, 131.0), Rating(1701.0, 136.0), 0.0) + " value decrease for player 2 losing") // get new rating value
//
//    println(GlickoRater.calcNewRating(Rating(1506.0, 131.0), Rating(1701.0, 136.0), 1.0) + " value increase for player 2 winning")
//    println(GlickoRater.calcNewRating(Rating(1701.0, 136.0), Rating(1701.0, 131.0), 0.0) + " value increase for player 1 losing")
//
//    // ^ need all 4 as each player has different increases and decreases in value for winning and losing
//
//    println("\nNew deviation")
//    println(GlickoRater.calcNewDeviation(Rating(1701.0, 136.0), Rating(1506.0, 131.0))) // number should be lower as certainty increases
//    println(GlickoRater.calcNewDeviation(Rating(1506.0, 131.0), Rating(1701.0, 136.0)))
//
//    val p1NewDev = GlickoRater.calcNewDeviation(Rating(1701.0, 136.0), Rating(1506.0, 131.0))
//    val p2NewDev = GlickoRater.calcNewDeviation(Rating(1506.0, 131.0), Rating(1701.0, 136.0))
//
//    println("\nDeviation decay")
//    println(GlickoRater.decayDeviation(p1NewDev, 1669855969)) // get decay'ed deviation after getting the game? or should do every hour and not care (probably the latter)?
//    println(GlickoRater.decayDeviation(p2NewDev, 1669855969))
//
//    println("\nExpected outcomes")
//    println(GlickoRater.calcExpectedOutcome(Rating(1701.0, 136.0), Rating(1506.0, 131.0)) * 100) // for cool win percentages
//    println(GlickoRater.calcExpectedOutcome(Rating(1506.0, 131.0), Rating(1701.0, 136.0)) * 100)
//
//    println(Utils.epochTimeNow)

    // println(Utils.deviationDecay)
    // Utils.deviationDecay.unsafeRunSync()
    // println(Database.updatePlayerDeviation(1001, 401.0).unsafeRunSync())
    // val timestampp = Requests.getLoginTimeStamp.unsafeRunSync()
    // println(timestampp)
    // Database.writeToDB(Utils.parseReplays(Requests.replayRequest(timestampp, 0, 10, 1))).unsafeRunSync()
    // IO(ExitCode.Success)
  }
}
