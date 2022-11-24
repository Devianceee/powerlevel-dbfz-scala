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
//import org.powerlevel.Main.loginTimestamp
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._

object Main extends IOApp.Simple {

//  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
//  val login_url = "https://dbf.channel.or.jp/api/user/login"

  val numberOfMatchesQueried = 1000 // better to do this via .conf file or some other environment way

  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + Utils.timeNow)))

  def replayResponseRank: Stream[IO, Any] = Stream.eval(IO(Database.writeToDB(for {
    r1 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 11))
    r2 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 501))
    r3 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 1001))
    r4 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 1501))
    r5 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 2001))
    r6 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 2501))
    r7 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 3001))
    r8 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 3501))
    r9 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 4001))
    r10 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 4501))
    r11 <- Utils.parseReplays(Requests.replayRequest(0, numberOfMatchesQueried, 5001))
  } yield r1 ++ r2 ++ r3 ++ r4 ++ r5 ++ r6 ++ r7 ++ r8 ++ r9 ++ r10 ++ r11).unsafeRunSync()))

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val every20Secs = Cron.unsafeParse("5,25,45 * * ? * *")
  val every15Mins = Cron.unsafeParse("0 0,15,30,45 * ? * *")

  val cronTasks = cronScheduler.schedule(List(
    every20Secs -> printForReplay,
    every20Secs -> replayResponseRank,
  ))

  println("Starting PowerLevel.info \nBy Deviance#3806\n\n")

  def run: IO[Unit] = {
    // TODO: run server which can be pinged to be able to cancel scheduled task and gracefully close database in case of maintenance
    // TODO: can probably add more flatMaps to places for better comprehension / less nesting (removes inner IO)
    // TODO: traverse keyword is very nice, see if I can use it in other places

    cronTasks.repeat.compile.drain.unsafeRunSync() // doesnt run without unsafeRunSync() why??
//    Database.writeToDB(Utils.parseReplays(Requests.replayRequest(loginTimestamp, 0, 10, 11))).unsafeRunSync()
    IO.unit
  }
}
