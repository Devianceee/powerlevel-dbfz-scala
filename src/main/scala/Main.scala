package org.powerlevel

import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.comcast.ip4s._
import io.circe.Encoder._
import io.circe.Json
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.ember.server._
//import play.api.libs.json._
import org.http4s.implicits._

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.util.Try

object Main extends IOApp.Simple {

//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  val numberOfMatchesQueried = 100 // better to do this via .conf file or some other environment way

  def getReplaySingle(timestamp:String, fromRank: Int) = Database.writeToDB(Utils.parseReplays(Requests.replayRequest(timestamp, 0, numberOfMatchesQueried, fromRank)))
//  def getUser(name: String) = Database.getUsersWithSimilarName(name) >> IO.println(s"${Thread.currentThread().getName} - Request Finished for User $name! Completed at: ${Utils.timeNow}")
  def replays(timestamp: String, fromRank: Int): IO[Unit] = getReplaySingle(timestamp, fromRank) >> IO.println(s"${Thread.currentThread().getName} - Request Finished for Rank $fromRank! Completed at: ${Utils.timeNow}")

//  val helloTest: IO[Unit] = IO(println("Hello")) >> IO.println(s"${Thread.currentThread().getName} - Request Finished! Completed at: ${Utils.timeNow}") >> IO.sleep(2.seconds) >> helloTest

  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

  val getRoot = Request[IO](Method.GET, uri"/")

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name. Time now is ${Utils.timeNow}")

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
      //      replays(reqTimestamp, 10501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 11001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 11501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 12001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 12501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 13001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 13501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 14001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 14501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 15001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 15501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 16001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 16501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 17001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 17501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 18001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 18501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 19001).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 19501).unsafeRunAsync(_ => ())
      //      replays(reqTimestamp, 20001).unsafeRunAsync(_ => ())


      // async calls to get replays all in one go in parallel

      Ok(s"Request sent at: ${Utils.timeNow}")

    case GET -> Root / "name" / name =>
      Ok(Utils.searchPlayer(s"%$name%"))

    case GET -> Root / "playerid" / player_id =>
      Ok(Utils.getPlayerGames(player_id.toLong))

    case _ =>
      Ok("Error")

  }.orNotFound

  val server: IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"7000")
    .withHttpApp(routes)
    .build
    .use(_ => IO.never)
    .as(IO.unit)

  override def run: IO[Unit] = {
    // TODO: run server which can be pinged to be able to cancel scheduled task and gracefully close database in case of maintenance
    // TODO: can probably add more flatMaps to places for better comprehension / less nesting (removes inner IO)
    // TODO: traverse keyword is very nice, see if I can use it in other places
    server
  }
}
