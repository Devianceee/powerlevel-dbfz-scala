package org.powerlevel

import cats.effect.{IO, _}
import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.Logger
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.UrlForm
import org.http4s.client.dsl.io._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits.http4sLiteralsSyntax

object Main extends IOApp.Simple {

//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  var loginTimestamp: String = "" // Yes I hate using a var too, I didn't want to do this too
  val numberOfMatchesQueried = 100

  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + Utils.timeNow)))
  val printForLogin: Stream[IO, Unit] = Stream.eval(IO(println("Login ping! The time is: " + Utils.timeNow)))
  val loginResponse: Stream[IO, Any] = Stream.eval(IO(loginTimestamp = Requests.getLoginTimeStamp))
  def replayResponseRank(fromRank: Int): Stream[IO, Any] = Stream.eval(IO(Utils.parseReplays(Requests.replayRequest(loginTimestamp, 0, numberOfMatchesQueried, fromRank), numberOfMatchesQueried)))

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val every21Secs = Cron.unsafeParse("1,21,41 * * ? * *")
  val every15Mins = Cron.unsafeParse("0 0,15,30,45 * ? * *")

  val cronTasks = cronScheduler.schedule(List(
    every21Secs -> printForReplay,
    every21Secs -> replayResponseRank(11),
    every21Secs -> replayResponseRank(101),
    every21Secs -> replayResponseRank(201),
    every21Secs -> replayResponseRank(301),
    every21Secs -> replayResponseRank(401),
    every21Secs -> replayResponseRank(501),
    every21Secs -> replayResponseRank(601),
    every21Secs -> replayResponseRank(701),
    every21Secs -> replayResponseRank(801),
    every21Secs -> replayResponseRank(901),
    every21Secs -> replayResponseRank(1001),
    every15Mins -> printForLogin,
    every15Mins -> loginResponse,
  ))

  // create case class to parse replay responses and
  // TODO: add a way to make sure duplicate matches aren't stored, probably some unique identifier
  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

  override def run: IO[Unit] = {
    loginTimestamp = Requests.getLoginTimeStamp
    println(loginTimestamp)
    cronTasks.attempt.compile.drain.unsafeRunSync()
    IO.unit
  }
}
