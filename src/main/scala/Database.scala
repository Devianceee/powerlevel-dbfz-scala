package org.powerlevel

import cats.effect.IO

object Database {
  def writeToDB(replayResults: IO[List[ReplayResults]]) = {
    replayResults.map{result =>
      result.map(println(_))
    }
  }
  IO.unit
}
