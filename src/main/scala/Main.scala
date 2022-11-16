package org.powerlevel

import cats.effect._
import cats.effect.unsafe.implicits.global
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.Method.{GET, POST}
import org.http4s.circe.jsonOf
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.headers.{Accept, Authorization}
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{AuthScheme, Credentials, EntityDecoder, MediaType, UrlForm}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
case class AuthResponse(access_token: String)

object Main extends IOApp.Simple {
  implicit val authResponseEntityDecoder: EntityDecoder[IO, AuthResponse] = jsonOf

  val printTime: Stream[IO, Unit] = Stream.eval(IO(println("Hello world! The time is: " +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))))
  val statement: Stream[IO, Unit] = Stream.eval(IO(if (56 > 0) println(100) else println(0)))
  val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
//  val example =
  val postRequest = POST(
    UrlForm(
      "data" -> "9295b2313830323035303733333032393434363233ad3633356661306433313734303802a5302e302e3303950701000a961cffff01ff90"
    ),
    uri"https://dbf.channel.or.jp/api/catalog/get_replay"
  )

  override def run: IO[Unit] = {
    val cronScheduler = Cron4sScheduler.systemDefault[IO]
//    val everyMinute = Cron.unsafeParse("1 * * ? * *")
    val every20Secs = Cron.unsafeParse("*/20 * * ? * *")
//    val tasks = cronScheduler.schedule(List(everyMinute -> printTime, everyMinute -> statement))
    val tasks = cronScheduler.schedule(List(every20Secs -> printTime, every20Secs -> statement))
    println(httpClient.expect[String](postRequest).unsafeRunSync())
    IO.unit
//    IO(println(example.unsafeRunSync()))
  }
}
