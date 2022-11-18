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

  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + Utils.timeNow)))
  val printForLogin: Stream[IO, Unit] = Stream.eval(IO(println("Login ping! The time is: " + Utils.timeNow)))
  val loginResponse: Stream[IO, Any] = Stream.eval(IO(loginTimestamp = Requests.getLoginTimeStamp))
  val replayResponse: Stream[IO, Any] = Stream.eval(IO(Utils.prettyPrintToScreen(Requests.replayRequest(loginTimestamp, 0, 1, 11))))

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val every25Secs = Cron.unsafeParse("*/25 * * ? * *")
  val every15Mins = Cron.unsafeParse("0 0,15,30,45 * ? * *")

  val cronTasks = cronScheduler.schedule(List(
    every25Secs -> printForReplay,
    every25Secs -> replayResponse,
    every15Mins -> printForLogin,
    every15Mins -> loginResponse,
  ))

  // create case class to parse replay responses and
  // TODO: add a way to make sure duplicate matches aren't stored, probably some unique identifier
  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

  override def run: IO[Unit] = {
    loginTimestamp = Requests.getLoginTimeStamp

    cronTasks.attempt.compile.drain.unsafeRunSync()
//    Utils.prettyPrintToScreen(Requests.replayRequest(Requests.getLoginTimeStamp, 0, 1, 11))
    IO.unit
  }
}
