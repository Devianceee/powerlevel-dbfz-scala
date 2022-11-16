package org.powerlevel

import cats.effect._
import cats.effect.unsafe.implicits.global
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main extends IOApp.Simple {
  val printTime: Stream[IO, Unit] = Stream.eval(IO(println("Hello world! The time is: " +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))))

  override def run: IO[Unit] = {
    val cronScheduler = Cron4sScheduler.systemDefault[IO]
    val evenSeconds = Cron.unsafeParse("*/120 * * ? * *")
    val scheduled = cronScheduler.awakeEvery(evenSeconds) >> printTime

    IO(scheduled.attempt.compile.drain.unsafeRunSync())
  }
}
