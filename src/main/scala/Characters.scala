package org.powerlevel

object Characters extends Enumeration {
  // create enum to represent characters which would get number from JSON response and match to the enum. refer to https://gist.github.com/wcolding/bbbc154ef0368b0519cfd9ca4308d386
  type Character = Value

  val Goku_SSJ, Vegeta_SSJ, Piccolo, Teen_Gohan, Frieza, Ginyu, Trunks, Cell, Android_18, Gotenks, Krillin, Kid_Buu, Majin_Boo, Nappa, Android_16, Yamcha, Tien, Adult_Gohan, Hit,
  Goku_SSGSS, Vegeta_SSGSS, Beerus, Goku_Black, Android_21, Android_21_Evil, Android_21_Good, Goku, Vegeta, Broly, Zamasu, Bardock, Vegito_SSGSS, Android_17, Cooler, Jiren,
  Videl, Goku_GT, Janemba, Gogeta_SSGSS, Broly_DBS, Kefla, Goku_Ultra_Instinct, Master_Roshi, Super_Baby_2, Gogeta_SS4, Android_21_Lab_Coat = Value
}