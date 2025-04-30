ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.2"

lazy val root = (project in file("."))
  .settings(
    name                 := "sangria-demo",
    Compile / run / fork := true,
    run / connectInput   := true,
    outputStrategy       := Some(StdoutOutput),
    run / javaOptions   ++= Seq(
      "-Dlogback.configurationFile=src/main/resources/logback.xml",
      "-Dlogback.debug=true",
    ),
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "clean; fmt; all compile Test/compile IntegrationTest/compile; test")

val sangriaVersion      = "4.1.0"
val sangriaCirceVersion = "1.3.2"
val circeVersion        = "0.14.9"
val http4sVersion       = "0.23.27"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria"             % sangriaVersion,
  "org.sangria-graphql" %% "sangria-circe"       % sangriaCirceVersion,
  "io.circe"            %% "circe-core"          % circeVersion,
  "io.circe"            %% "circe-generic"       % circeVersion,
  "io.circe"            %% "circe-parser"        % circeVersion,
  "org.typelevel"       %% "log4cats-slf4j"      % "2.6.0",
  "ch.qos.logback"       % "logback-classic"     % "1.5.6",
  "org.scalatest"       %% "scalatest"           % "3.2.18" % Test,
  "org.http4s"          %% "http4s-ember-server" % http4sVersion,
  "org.http4s"          %% "http4s-ember-client" % http4sVersion,
  "org.http4s"          %% "http4s-dsl"          % http4sVersion,
  "org.http4s"          %% "http4s-circe"        % http4sVersion,
  "com.comcast"         %% "ip4s-core"           % "3.7.0",
  "org.http4s"          %% "http4s-server"       % http4sVersion,
)

scalacOptions ++= Seq(
  "-deprecation",
  "-explain",
  "-explain-types",
  "-feature",
  "-indent",
  "-new-syntax",
  "-print-lines",
  "-unchecked",
  "-Xkind-projector",
  "-Werror",
  "-Xmigration",
  "-explain-cyclic",
)
