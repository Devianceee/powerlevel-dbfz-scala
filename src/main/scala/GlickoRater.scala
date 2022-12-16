package org.powerlevel

import scala.math._

case class Rating(value: Double, deviation: Double)

object GlickoRater {
  val RATING_PERIOD = 60 * 60
  val INITIAL_DEVIATION = 700.0
  val C = 3.1
  val Q = 0.00575646273


  def calcG(ratingDeviation: Double) = {
    1.0 / sqrt(1.0 + (3.0 * pow(Q, 2.0) * pow(ratingDeviation, 2.0)) / pow(Pi, 2.0))
  }

  def calcE(ownRating: Rating, otherRating: Rating) = {
    val ratingDifference = ownRating.value - otherRating.value
    val g = calcG(ownRating.deviation)

    1.0 / (1.0 + pow(10.0, g * ratingDifference / -400.0))
  }

  def calcD2(ownRating: Rating, otherRating: Rating) = {
    val g = calcG(ownRating.deviation);
    val e = calcE(ownRating, otherRating);

    1.0 / (pow(Q, 2.0) * pow(g, 2.0) * e * (1.0 - e))
  }

  def calcNewRating(own_rating: Rating, other_rating: Rating, outcome: Double) = {
    val g = calcG(other_rating.deviation)
    val e = calcE(own_rating, other_rating)
    val d2 = calcD2(own_rating, other_rating)

    own_rating.value + ((Q / (1.0 / pow(own_rating.deviation, 2.0) + (1.0 / d2))) * g * (outcome - e))
  }

  def calcNewDeviation(ownRating: Rating, otherRating: Rating) = {
    val d2 = calcD2(ownRating, otherRating);

    max(25.0, sqrt(1.0 / ((1.0 / pow(ownRating.deviation, 2.0)) + (1.0 / d2))))
  }

  def decayDeviation(deviation: Double, previousGameTimestamp: Int) = {
    val decayCount = (Utils.epochTimeNow - previousGameTimestamp) / RATING_PERIOD // get time difference between their last game and now divided by the rating period (set to 1 hour)
    // println(decayCount)
    var updatedDeviation = deviation
    for (_ <- 0 to decayCount.toInt) {
      updatedDeviation = sqrt(pow(updatedDeviation, 2.0) + pow(C, 2.0))
      updatedDeviation = min(updatedDeviation, INITIAL_DEVIATION)
    }
    updatedDeviation
  }

  def calcExpectedOutcome(own_rating: Rating, other_rating: Rating) = { // to use
    val rating_difference = own_rating.value - other_rating.value;

    val g = calcG(sqrt(
      pow(own_rating.deviation, 2.0) + pow(other_rating.deviation, 2.0),
    ));

    1.0 / (1.0 + pow(10.0, g * rating_difference / -400.0))
  }

}
