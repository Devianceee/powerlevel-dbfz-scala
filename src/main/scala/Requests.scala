package org.powerlevel

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.Method.POST
import org.http4s.UrlForm
import org.http4s.client.dsl.io._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits.http4sLiteralsSyntax
import com.typesafe.scalalogging.Logger
import play.api.libs.json.{JsValue, Json}

case class ReplayResults(uniqueMatchID: Long, matchTime: String,
                         winnerID: Long, winnerName: String, winnerCharacters: List[String],
                         loserID: Long, loserName: String, loserCharacters: List[String])


object Requests {

  private def loginRequest(): String = {
    val httpClient: Client[IO] = JavaNetClientBuilder[IO].create

    val loginJson = """[["", "", 2,"0.0.3", 3],["76561198077238939", "110000106f8de9b", 256, 0]]"""
    val postRequest = POST (UrlForm("data" -> Utils.packJson(loginJson)), uri"https://dbf.channel.or.jp/api/user/login")
    Utils.unpackResponse(httpClient.expect[Array[Byte]](postRequest).unsafeRunSync())
  }

  def replayRequest(timestamp: String, replayPages: Int, numberOfMatchesQueried: Int, fromRank: Int, character: Int = -1): String = {
    println("Getting Replays")

    val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
    val replayJson = s"""[
                        |    [
                        |        "180205073302944623",
                        |        "$timestamp",
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
                        |            102,
                        |            $fromRank,
                        |            -1,
                        |            [
                        |            ]
                        |        ]
                        |    ]
                        |]""".stripMargin

    val postRequest = POST (UrlForm("data" -> Utils.packJson(replayJson)), uri"https://dbf.channel.or.jp/api/catalog/get_replay")
    Utils.unpackResponse(httpClient.expect[Array[Byte]](postRequest).unsafeRunSync())
  }

  def getLoginTimeStamp: String = {
    println("Getting Login Timestamp...")
    val jsonList: JsValue = Json.parse(loginRequest()).as[List[JsValue]].head(0)

    println("Obtained Login Timestamp!")
    return jsonList.toString().replace("\"", "")
  }
}
