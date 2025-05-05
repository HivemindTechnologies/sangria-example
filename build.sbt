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
val circeVersion        = "0.14.10"
val ZIOVersion          = "2.1.10"
val zioHttpVersion      = "3.0.1"
val zioLoggingVersion   = "2.2.1"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria"           % sangriaVersion,
  "org.sangria-graphql" %% "sangria-circe"     % sangriaCirceVersion,
  "io.circe"            %% "circe-core"        % circeVersion,
  "io.circe"            %% "circe-generic"     % circeVersion,
  "io.circe"            %% "circe-parser"      % circeVersion,
  "ch.qos.logback"       % "logback-classic"   % "1.5.6",
  "org.scalatest"       %% "scalatest"         % "3.2.18" % Test,
  "dev.zio"             %% "zio"               % ZIOVersion,
  "dev.zio"             %% "zio-http"          % zioHttpVersion,
  "dev.zio"             %% "zio-logging"       % zioLoggingVersion,
  "dev.zio"             %% "zio-logging-slf4j" % zioLoggingVersion,
)

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

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
