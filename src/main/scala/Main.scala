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
import org.http4s.{EntityDecoder, UrlForm}
import org.velvia.MsgPack
import org.velvia.msgpack.RawStringCodecs.StringCodec
import org.velvia.msgpack._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.bind.DatatypeConverter

object Main extends IOApp.Simple {
  val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
  val printTime: Stream[IO, Unit] = Stream.eval(IO(println("Hello world! The time is: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))))
  val statement: Stream[IO, Unit] = Stream.eval(IO(if (56 > 0) println(100) else println(0)))
  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val everyMinute = Cron.unsafeParse("1 * * ? * *")
  val every20Secs = Cron.unsafeParse("*/20 * * ? * *")
  val tasks = cronScheduler.schedule(List(every20Secs -> printTime, every20Secs -> statement))

  def convertBytesToHex(bytes: Array[Byte]): String = {
    val sb = new StringBuilder
    for (b <- bytes) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  //    val loginJson = parse("""["", "", 2,"0.0.3", 3],["76561198077238939", "110000106f8de9b", 256, 0]""")

  val loginRequest = POST(
    UrlForm(
      "data" -> "dd00000002dd00000005a0a002a5302e302e3303dd00000004b13736353631313938303737323338393339af313130303030313036663864653962cd010000"
    ),
    uri"https://dbf.channel.or.jp/api/user/login"
  )

  override def run: IO[Unit] = {

    val response = httpClient.expect[Array[Byte]](loginRequest).unsafeRunSync()
    val unpacked = MsgPack.unpack(response).toString
    println(unpacked)

    val test = pack("test")
    println(unpack[String](DatatypeConverter.parseHexBinary(convertBytesToHex(test))))
    IO.unit
  }
}
