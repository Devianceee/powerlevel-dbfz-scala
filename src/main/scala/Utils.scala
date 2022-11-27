package org.powerlevel

import cats.effect.IO
import play.api.libs.json._
import wvlet.airframe.msgpack.spi.MessagePack

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import scala.collection.immutable.::
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._


object Utils {

  def timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

  def timestampToEpoch(s: JsValue): Long = {
    LocalDateTime.parse(s.toString.replace("\"", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC)
  }

  def epochToTime(epoch: String): String = {
    LocalDateTime.ofEpochSecond(epoch.toLong, 0, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
  }

  def jpTimeParse(matchTime: String): String = {
    val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

    val jpTime = LocalDateTime.parse(matchTime, formatter).atZone(ZoneId.of("Asia/Tokyo"))
    val currentTime = jpTime.toOffsetDateTime.withOffsetSameInstant(ZonedDateTime.now().getOffset).format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"))
    currentTime
  }

  def searchPlayer(name: String) = {
    val players = Database.searchPlayer(name).map { players =>
      players.map { player =>
        Player(player._1, player._2)
      }
    }

    players.map { listOfPlayers =>
      listOfPlayers.map {player =>
        player.asJson
      }
      listOfPlayers.asJson
    }
  }

  def getPlayerGames(player_id: Long) = {
    val games = Database.getPlayerGames(player_id).map { games =>
      games.map { game =>
        PlayerGames(epochToTime(game._1), game._2, game._3, game._4, game._5)
      }
    }

    games.map { listOfGames =>
      listOfGames.map { game =>
        game.asJson
      }
      listOfGames.asJson
    }
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
    } yield MessagePack.newUnpacker(x).unpackValue.toJson
  }

  def parseCharacters(chars: JsValue): List[String] = {
    List(Characters(chars(0).toString().toInt).toString,
      Characters(chars(1).toString().toInt).toString, Characters(chars(2).toString().toInt).toString)
  }

  def parseReplays(response: IO[String]) = { // really ugly parsing I'm sorry, blame ArcSys for not keying their json

    val jsonList: IO[JsValue] = for {
      parse <- response
//      _ = println(parse)
    } yield Json.parse(parse).as[List[List[JsValue]]].tail.head(2)

    /*
    TODO: Error checking in case of malformed response,
     Try with Case Success or Failure with Failure returning Failure and being handled by Database.writeToDB()
    */
    val matches: IO[List[ReplayResults]] = jsonList.map{ jsList =>
      jsList.as[List[JsValue]].map(wholeMatch => // each whole match
        wholeMatch.as[List[JsValue]] match {
          case rawID :: _ :: _ :: rawWinnerCharacters :: rawLoserCharacters :: rawWinnerPlayer :: rawLoserPlayer :: _ :: rawMatchTime :: _  =>
            (ReplayResults(rawID.toString.replace("\"", "").toLong, timestampToEpoch(rawMatchTime), // Match ID, Match Date&Time
              rawWinnerPlayer.head(0).toString.replace("\"", "").toLong, rawWinnerPlayer.head(1).toString.replace("\"", ""), parseCharacters(rawWinnerCharacters), // Winner ID, name and characters
              rawLoserPlayer.head(0).toString.replace("\"", "").toLong, rawLoserPlayer.head(1).toString.replace("\"", ""), parseCharacters(rawLoserCharacters))) // Loser ID, name and characters
        })
    }
//    Database.writeToDB(matches)
    matches
  }



}
