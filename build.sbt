ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"
val http4sVersion = "0.23.10"
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "eu.timepit" %% "fs2-cron-cron4s" % "0.7.2", //and/or
  "eu.timepit" %% "fs2-cron-calev" % "0.7.2",
  "org.typelevel" %% "cats-effect" % "3.4.0",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.velvia" %% "msgpack4s" % "0.6.0",
  "com.google.protobuf" % "protobuf-java-util" % "4.0.0-rc-2",
  "org.json4s" %% "json4s-native" % "3.5.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "io.circe" %% "circe-generic" % "0.14.3",
  "io.circe" %% "circe-literal" % "0.14.3"
)

lazy val root = (project in file("."))
  .settings(
    name := "powerlevel-dbfz-scala",
    idePackagePrefix := Some("org.powerlevel")
  )
