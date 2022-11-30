ThisBuild / version := "0.1.0-SNAPSHOT"

//ThisBuild / scalaVersion := "2.13.10"
ThisBuild / scalaVersion := "3.2.0"
val http4sVersion = "0.23.10"
val DoobieVersion = "1.0.0-RC1"
val NewTypeVersion = "0.4.4"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("asflierl", "maven")

//enablePlugins(JavaAppPackaging)
//addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.4.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.3",
  "io.circe" %% "circe-parser" % "0.14.3",
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.wvlet.airframe" %% "airframe-msgpack" % "22.11.1",
//  "com.typesafe.play" %% "play-json" % "2.9.3",
  "com.typesafe.play" %% "play-json" % "2.10.0-RC7",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
//  "io.estatico" %% "newtype" % NewTypeVersion,
//  "eu.flierl" %% "sglicko2" % "1.7.1",
  "eu.flierl" %% "sglicko2" % "3.0.0",
  "org.typelevel" %% "cats-effect-cps" % "0.4.0",

)

lazy val root = (project in file("."))
  .settings(
    name := "powerlevel-dbfz-scala",
    idePackagePrefix := Some("org.powerlevel")
  )

//maintainer := "Deviance"