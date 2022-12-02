package org.powerlevel

import cats.effect.IO
import play.api.libs.json.*
import wvlet.airframe.msgpack.spi.MessagePack

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import scala.collection.immutable.::
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import sglicko2.*
import sglicko2.WinOrDraw.*
import sglicko2.WinOrDraw.Ops.*


object Utils {
  given Glicko2 = Glicko2(tau = Tau[0.3d], defaultVolatility = Volatility(0.03d), scale = Scale.Glicko)

  def timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

  def timestampToEpoch(s: JsValue): Long = {
    LocalDateTime.parse(s.toString.replace("\"", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.UTC)
  }

  def bootUpdateEntireGlickoLeaderboard: Leaderboard[Long] = { // do this on boot AND after every time we get replays
    import cats.effect.unsafe.implicits.global

    val allPlayers = Database.getAllPlayersLastGlicko.unsafeRunSync()
    val formattedPlayers = allPlayers.map { player =>
      Player(player._1, Rating(player._2), Deviation(player._3))
    }
    val leaderboard = Leaderboard.Empty
    println(leaderboard.rankedPlayers)
    val leaderboard2: Leaderboard[Long] = Leaderboard.fromPlayers(formattedPlayers)
    println(leaderboard2.rankedPlayers)
    println("Leaderboard updated")
    leaderboard2
  }

//  def updateEntireGlickoLeaderboardAfterReplays(): Leaderboard[Long] = { // do this on boot AND after every time we get replays
//    import cats.effect.unsafe.implicits.global
//
//    val allPlayers = Database.getAllPlayersLastGlicko.unsafeRunSync()
//    val formattedPlayers = allPlayers.map { player =>
//      Player(player._1, Rating(player._2), Deviation(player._3))
//    }
//    val leaderboard = Leaderboard.Empty
//    println(leaderboard.rankedPlayers)
//    val leaderboard2: Leaderboard[Long] = Leaderboard.fromPlayers(formattedPlayers)
//    println(leaderboard2.rankedPlayers)
//    println("Leaderboard updated")
//    leaderboard2
//  }

  def glickoUpdateGames(winnerID: Long, loserID: Long, leaderboard: Leaderboard[Long]): Leaderboard[Long] = {
    // val updatedLeaderboard after (id)
    val updatedLeaderboard: Leaderboard[Long] = leaderboard.after(RatingPeriod(winnerID winsVs loserID))
    updatedLeaderboard
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
        DBPlayer(player._1, player._2, (Utils.epochToTime(player._3) + " UTC"), player._4, player._5)
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
        PlayerGames(epochToTime(game._1), game._2, game._3, game._4, game._5, game._6, game._7, game._8, game._9)
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
//      _ = println(parse) // print for debug
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
    val reversedMatches = for {
      x <- matches
    } yield x.reverse
    reversedMatches
  }



}
