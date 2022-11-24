package org.powerlevel

import cats.effect.IO.executionContext
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, _}

import scala.concurrent.ExecutionContext

import com.comcast.ip4s._
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import fs2.concurrent.SignallingRef
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.UrlForm
import org.http4s.client.dsl.io._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import org.powerlevel.Main.loginTimestamp
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._

object Main extends IOApp.Simple {

//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  var loginTimestamp: String = Requests.getLoginTimeStamp.unsafeRunSync() // Yes I hate using a var too, I didn't want to do this too
  var cronLoginTimestamp: IO[String] = IO[String]("") // Yes I hate using a var too, I didn't want to do this too
  val numberOfMatchesQueried = 200 // better to do this via .conf file or some other environment way

  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + Utils.timeNow)))
  val printForLogin: Stream[IO, Unit] = Stream.eval(IO(println("Login ping! The time is: " + Utils.timeNow)))

  val loginResponse: Stream[IO, Any] = {
    Stream.eval(IO(loginTimestamp = Requests.getLoginTimeStamp.unsafeRunSync()))
  }

  def replayResponseRank(fromRank: Int): Stream[IO, Any] = Stream.eval(IO(
    Database.writeToDB(Utils.parseReplays(Requests.replayRequest(loginTimestamp, 0, numberOfMatchesQueried, fromRank))).unsafeRunSync()
    )
  )

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val every20Secs = Cron.unsafeParse("*/20 * * ? * *")
  val every20SecsSecond = Cron.unsafeParse("4,24,44 * * ? * *")
  val every20SecsThird = Cron.unsafeParse("8,28,48 * * ? * *")
  val every15Mins = Cron.unsafeParse("0 0,15,30,45 * ? * *")

  val cronTasks = cronScheduler.schedule(List(
    every20Secs -> printForReplay,
    every20Secs -> replayResponseRank(11),
    every20Secs -> replayResponseRank(101),
    every20Secs -> replayResponseRank(201),
    every20Secs -> replayResponseRank(301),
    every20Secs -> replayResponseRank(401),
    every20Secs -> replayResponseRank(501),
    every20Secs -> replayResponseRank(601),
    every20Secs -> replayResponseRank(701),
    every20Secs -> replayResponseRank(801),
    every20Secs -> replayResponseRank(901),
    every20Secs -> replayResponseRank(1001),
    every20SecsSecond -> replayResponseRank(1101),
    every20SecsSecond -> replayResponseRank(1101),
    every20SecsSecond -> replayResponseRank(1201),
    every20SecsSecond -> replayResponseRank(1301),
    every20SecsSecond -> replayResponseRank(1401),
    every20SecsSecond -> replayResponseRank(1501),
    every20SecsSecond -> replayResponseRank(1601),
    every20SecsSecond -> replayResponseRank(1701),
    every20SecsSecond -> replayResponseRank(1801),
    every20SecsSecond -> replayResponseRank(1901),
    every20SecsSecond -> replayResponseRank(2001),
    every20SecsSecond -> replayResponseRank(2101),
    every20SecsSecond -> replayResponseRank(2201),
    every20SecsSecond -> replayResponseRank(2301),
    every20SecsSecond -> replayResponseRank(2401),
    every20SecsSecond -> replayResponseRank(2501),
    every20SecsSecond -> replayResponseRank(2601),
    every20SecsSecond -> replayResponseRank(2701),
    every20SecsSecond -> replayResponseRank(2801),
    every20SecsSecond -> replayResponseRank(2901),
    every20SecsThird -> replayResponseRank(3001),
    every20SecsThird -> replayResponseRank(3101),
    every20SecsThird -> replayResponseRank(3101),
    every20SecsThird -> replayResponseRank(3201),
    every20SecsThird -> replayResponseRank(3301),
    every20SecsThird -> replayResponseRank(3401),
    every20SecsThird -> replayResponseRank(3501),
    every20SecsThird -> replayResponseRank(3601),
    every20SecsThird -> replayResponseRank(3701),
    every20SecsThird -> replayResponseRank(3801),
    every20SecsThird -> replayResponseRank(3901),
    every20SecsThird -> replayResponseRank(4001),
    every15Mins -> printForLogin,
    every15Mins -> loginResponse,
  ))

  // create case class to parse replay responses and
  // TODO: add a way to make sure duplicate matches aren't stored, probably some unique identifier
  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

//  def createServer() = {
//    for {
//      loginTimestamp <- Requests.getLoginTimeStamp
////      _ <- cronTasks.attempt
//      val helloWorldService = HttpRoutes.of[IO] {
//        case GET -> Root =>
//          Ok(s"Hello!")
//      }
//
//      val httpApp = Router("/" -> helloWorldService).orNotFound
//      server =
//        EmberServerBuilder.default[IO].withHost(ipv4"0.0.0.0").withPort(port"8080").withHttpApp(httpApp)
//      _ = server.build.use(_ => IO.never)
//    } yield server
//  }

  def run: IO[Unit] = {
    // TODO: run server which can be pinged to be able to cancel scheduled task and gracefully close database in case of maintenance
    // TODO: can probably add more flatMaps to places for better comprehension / less nesting (removes inner IO)
    // TODO: traverse keyword is very nice, see if I can use it in other places

//    loginTimestamp = Requests.getLoginTimeStamp.unsafeRunSync() // not sure best way to do this without blocking first
    cronTasks.repeat.compile.drain.unsafeRunSync() // doesnt run without unsafeRunSync() why??
//    Utils.parseReplays(Requests.replayRequest(loginTimestamp, 0, 1000, 11)).unsafeRunSync()
    IO.unit
  }
}
