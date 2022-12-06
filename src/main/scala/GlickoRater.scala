package org.powerlevel

class GlickoRater {
  val DEFAULT_INITIAL_DEVIATION = 700
  val RATING_PERIOD_LENGTH = ???
  val Q: Double = 0.00575646273

  def decayDeviation(rating: Double, timeElapsed: Int) = ???
  def calcG(ratingDeviation: Double) = ???
  def calcE() = ???
  def calcD2() = ???
  def calcNewRating() = ???
  def calcNewDeviation() = ???
  def calcExpectedOutcome() = ???

}
