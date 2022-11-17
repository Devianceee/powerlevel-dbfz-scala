package org.powerlevel

import cats.effect._
import cats.effect.unsafe.implicits.global
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Request, UrlForm}
import org.json4s._
import wvlet.airframe.msgpack.spi.MessagePack
import org.http4s.Uri

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main extends IOApp.Simple {
  val get_replay_url = "https://dbf.channel.or.jp/api/catalog/get_replay"
  val login_url = "https://dbf.channel.or.jp/api/user/login"

  val timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  val printForReplay: Stream[IO, Unit] = Stream.eval(IO(println("Replay ping! The time is: " + timeNow)))
  val printForLogin: Stream[IO, Unit] = Stream.eval(IO(println("Login world! The time is: " + timeNow)))

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val everyMinute = Cron.unsafeParse("1 * * ? * *")
  val every30Mins = Cron.unsafeParse("3 * * ? * *")

  val tasks = cronScheduler.schedule(List(everyMinute -> printForReplay, every30Mins -> printForLogin))

  def convertBytesToHex(bytes: Array[Byte]): String = {
    val sb = new StringBuilder
    for (b <- bytes) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  def packJson(json: String): String = {
    convertBytesToHex(MessagePack.fromJSON(json))
  }

  def unpackResponse(response: Array[Byte]): String = {
    MessagePack.newUnpacker(response).unpackValue.toJson
  }

  def loginRequest(): Array[Byte] = {
    val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
    val loginJson = """[["", "", 2,"0.0.3", 3],["76561198077238939", "110000106f8de9b", 256, 0]]"""
    val postRequest = POST (UrlForm("data" -> packJson(loginJson)), uri"https://dbf.channel.or.jp/api/user/login")
    httpClient.expect[Array[Byte]](postRequest).unsafeRunSync()
  }

  def replayRequest(json: String): Array[Byte] = {
    val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
    val postRequest = POST (UrlForm("data" -> packJson(json)), uri"https://dbf.channel.or.jp/api/catalog/get_replay")
    httpClient.expect[Array[Byte]](postRequest).unsafeRunSync()
  }

  override def run: IO[Unit] = {
    println(unpackResponse(loginRequest()))

    IO.unit
  }
}
