package org.powerlevel

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.UrlForm
import org.http4s.client.dsl.io.*
import org.http4s.ember.client.*
import org.http4s.implicits.uri
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.DurationInt

object Requests {

  private def loginRequest(): IO[String] = {
    //76561199056721807, 1100001415a978f
    val loginJson = """[["", "", 2,"0.0.3", 3],["76561198077238939", "110000106f8de9b", 256, 0]]"""
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
//221127003353744044
    val replayJson = s"""[
                        |    [
                        |        "180205073302944623",
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

    val postRequest = POST (UrlForm("data" -> Utils.packJson(replayJson)), uri"https://dbf.channel.or.jp/api/catalog/get_replay")
    val client = EmberClientBuilder.default[IO].build.use { client =>
      client.expect[Array[Byte]](postRequest)
    }
    Utils.unpackResponse(client)
  }

  def getLoginTimeStamp: IO[String] = {
    val jsonList: IO[String] = for {
      request <- getJson(loginRequest())
    } yield request.replace("\"", "")
    jsonList
  }
}
