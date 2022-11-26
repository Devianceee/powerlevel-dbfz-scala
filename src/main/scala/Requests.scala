package org.powerlevel

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.UrlForm
import org.http4s.client.dsl.io._
import org.http4s.ember.client._
import org.http4s.implicits.http4sLiteralsSyntax
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.DurationInt



object Requests {

  private def loginRequest(): IO[String] = {
    val loginJson = """[["", "", 2,"0.0.3", 3],["76561199056721807", "1100001415a978f", 256, 0]]"""
    val postRequest = POST (UrlForm("data" -> Utils.packJson(loginJson)), uri"https://dbf.channel.or.jp/api/user/login")
    val client = EmberClientBuilder.default[IO].build.use { client =>
      client.expect[Array[Byte]](postRequest)
    }
    Utils.unpackResponse(client)
  }

  private def getJson(response: IO[String]) = {
    val foo: IO[String] = for {
      parse <- response
    } yield Json.parse(parse).as[List[JsValue]].head(0).toString()
    foo
  }

  def replayRequest(time: String, replayPages: Int, numberOfMatchesQueried: Int, fromRank: Int, character: Int = -1): IO[String] = {
//    val time = Requests.getLoginTimeStamp.unsafeRunSync()

//    if (time.length < 6) {
//      time = Requests.getLoginTimeStamp.unsafeRunSync()
//    }

    val replayJson = s"""[
                        |    [
                        |        "221127003353744044",
                        |        "$time",
                        |        2,
                        |        "0.0.3",
                        |        3
                        |    ],
                        |    [
                        |        7,
                        |        1,
                        |        $replayPages,
                        |        $numberOfMatchesQueried,
                        |        [
                        |            28,
                        |            $character,
                        |            104,
                        |            $fromRank,
                        |            -1,
                        |            [
                        |            ]
                        |        ]
                        |    ]
                        |]""".stripMargin

//    println("Timestamp in Requests.scala", replayJson)
    val postRequest = POST (UrlForm("data" -> Utils.packJson(replayJson)), uri"https://dbf.channel.or.jp/api/catalog/get_replay")
//    Utils.unpackResponse(httpClient.expect[Array[Byte]](postRequest))
    val client = EmberClientBuilder.default[IO].build.use { client =>
      client.expect[Array[Byte]](postRequest)
    }
    Utils.unpackResponse(client)
  }

  def getLoginTimeStamp: IO[String] = {
    val jsonList: IO[String] = for {
//      _ <- IO.pure(println("Getting Login Timestamp..."))
      request <- getJson(loginRequest())
//      _ = println("Obtained Login Timestamp!")
    } yield request.replace("\"", "")
    jsonList
  }
}
