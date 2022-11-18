ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"
val http4sVersion = "0.23.10"
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "eu.timepit" %% "fs2-cron-cron4s" % "0.7.2", //and/or
  "org.typelevel" %% "cats-effect" % "3.4.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.wvlet.airframe" %% "airframe-msgpack" % "22.11.1",
  "io.spray" %% "spray-json" % "1.3.5",
)

lazy val root = (project in file("."))
  .settings(
    name := "powerlevel-dbfz-scala",
    idePackagePrefix := Some("org.powerlevel")
  )
