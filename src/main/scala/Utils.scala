package org.powerlevel

import wvlet.airframe.msgpack.spi.MessagePack
import spray.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {

  def timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

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

  def jsonToList(json: String): String = {
    json //todo
  }

  def prettyPrintToScreen(response: String) = {
    println(response.parseJson.prettyPrint)
  }

}
