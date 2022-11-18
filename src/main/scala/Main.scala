package org.powerlevel

import cats.effect._
import cats.effect.unsafe.implicits.global
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main extends IOApp.Simple {
//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  var loginTimestamp: String = "" // Yes I hate using a var too, I didn't want to do this too
  def setTimestamp(x: String) = loginTimestamp = x

  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + Utils.timeNow)))
  val printForLogin: Stream[IO, Unit] = Stream.eval(IO(println("Login ping! The time is: " + Utils.timeNow)))
  val loginResponse: Stream[IO, Any] = Stream.eval(IO(loginTimestamp = Requests.getLoginTimeStamp))
  val replayResponse: Stream[IO, String] = Stream.eval(IO(Requests.replayRequest(loginTimestamp, 0, 10, 11)))

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val every30Secs = Cron.unsafeParse("*/30 * * ? * *")
  val every30Mins = Cron.unsafeParse("0 0,30 * ? * *")

  val tasks = cronScheduler.schedule(List(
    every30Secs -> printForReplay,
    every30Secs -> replayResponse,

    every30Mins -> printForLogin,
    every30Mins -> loginResponse
  ))

  override def run: IO[Unit] = {
    println("Starting PowerLevel.info...\n By Deviance#3806")
    tasks.attempt.compile.drain.unsafeRunSync()


    // create case class to parse replay responses and

    IO.unit
  }
}
