package org.powerlevel

import cats.effect.IO
import play.api.libs.json._
import wvlet.airframe.msgpack.spi.MessagePack

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

object Utils {

  def timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

  def jpTimeParse(matchTime: String): String = {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

    val jpTime = LocalDateTime.parse(matchTime, formatter).atZone(ZoneId.of("Asia/Tokyo"))
    val currentTime = jpTime.toOffsetDateTime.withOffsetSameInstant(ZonedDateTime.now().getOffset).format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"))
    currentTime
  }

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

  def unpackResponse(response: IO[Array[Byte]]): IO[String] = {
    for {
      x <- response
      unpacked <- IO(MessagePack.newUnpacker(x).unpackValue.toJson)
    } yield unpacked
  }

  def parseReplays(response: String, numberOfMatchesQueried: Int): Unit = { // really ugly parsing I'm sorry, blame ArcSys for not keying their json
    val jsonList: JsValue = (Json.parse(response).as[List[JsValue]]).tail.head(2)
    // first value in list is the match index in the response. for example: jsonList(y)(3)), y being the match index

    for (i <- 0 until numberOfMatchesQueried) {
      val matchID = jsonList(i)(0).toString.toLong
      val matchTimestamp = jsonList(i)(8).toString.replace("\"", "")

      val winnerPlayerID = jsonList(i)(5)(0)(0).toString.replace("\"", "").toLong
      val winnerPlayerName = jsonList(i)(5)(0)(1).toString.replace("\"", "")
      val winnerCharacters = List[String](Characters(jsonList(i)(3)(0).toString().toInt).toString, Characters(jsonList(i)(3)(1).toString().toInt).toString, Characters(jsonList(i)(3)(2).toString().toInt).toString)

      val loserPlayerID = jsonList(i)(6)(0)(0).toString.replace("\"", "").toLong
      val loserPlayerName = jsonList(i)(6)(0)(1).toString().replace("\"", "")
      val loserCharacters = List[String](Characters(jsonList(i)(4)(0).toString().toInt).toString, Characters(jsonList(i)(4)(1).toString().toInt).toString, Characters(jsonList(i)(4)(2).toString().toInt).toString)

//      println(s"Unique match ID: $matchID")
//      println(s"Match Timestamp: $matchTimestamp\n")
//
//      println(s"Winner Player ID:  $winnerPlayerID")
//      println(s"Winner Player Name: $winnerPlayerName")
//      println(s"Winner Characters:  $winnerCharacters\n")
//
//      println(s"Loser Player ID: $loserPlayerID")
//      println(s"Loser Player Name: $loserPlayerName")
//      println(s"Loser Characters: $loserCharacters\n")

      Database.writeToDB(ReplayResults(matchID, matchTimestamp, winnerPlayerID, winnerPlayerName, winnerCharacters, loserPlayerID, loserPlayerName, loserCharacters))
    }
    println("Replay ping! The time is: " + Utils.timeNow)
  }
}
