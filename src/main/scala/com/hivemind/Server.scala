package com.hivemind

import cats.effect.*
import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.server.Router
import org.http4s.circe.*
import org.http4s.server.staticcontent.*
import org.http4s.headers.`Content-Type`
import sangria.execution.*
import sangria.marshalling.circe.*
import sangria.parser.{QueryParser, SyntaxError}
import sangria.ast.Document
import com.hivemind.schema.SchemaDefinition
import com.hivemind.models.Book
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.ci.CIString
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import cats.data.OptionT
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import java.io.InputStream
import scala.io.Source

object Server extends IOApp {
  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  implicit val logger: Logger[IO]               = loggerFactory.getLogger
  implicit val bookEncoder: Encoder[Book]       = deriveEncoder[Book]

  val corsHeaders = Headers(
    Header.Raw(CIString("Access-Control-Allow-Origin"), "*"),
    Header.Raw(CIString("Access-Control-Allow-Methods"), "GET, POST, OPTIONS"),
    Header.Raw(CIString("Access-Control-Allow-Headers"), "Content-Type, Accept, Origin"),
    Header.Raw(CIString("Access-Control-Max-Age"), "3600"),
  )

  val graphQLService = HttpRoutes.of[IO] {
    case req @ POST -> Root / "graphql" =>
      for {
        _            <- logger.info(s"Received GraphQL request: ${req.method} ${req.uri}")
        body         <- req.as[String]
        json         <- IO.fromEither(parse(body))
        query         = json.hcursor.downField("query").as[String].getOrElse("")
        variables     = json.hcursor.downField("variables").as[Json].getOrElse(Json.obj())
        operationName = json.hcursor.downField("operationName").as[String].toOption
        queryAst     <- IO.fromTry(QueryParser.parse(query))
        queryResult  <- IO.fromFuture(
                          IO.delay(
                            Executor.execute(
                              SchemaDefinition.schema,
                              queryAst,
                              (),
                              variables = variables,
                              operationName = operationName,
                            ),
                          ),
                        )
        resp         <- Ok(queryResult).map(_.withHeaders(corsHeaders))
      } yield resp

    case OPTIONS -> Root / "graphql" =>
      logger.info("Received OPTIONS request for /graphql") >> Ok().map(_.withHeaders(corsHeaders))

    case GET -> Root / "graphiql" =>
      for {
        _    <- logger.info("Serving GraphiQL interface")
        html <- IO.blocking {
                  val stream: InputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream("graphiql.html")
                  Source.fromInputStream(stream).mkString
                }
        resp <- Ok(html).map(_.withContentType(`Content-Type`(MediaType.text.html)).withHeaders(corsHeaders))
      } yield resp
  }

  def run(args: List[String]): IO[ExitCode] = {
    val port = port"8081"
    val host = host"localhost"
    val app  = Router(
      "/" -> graphQLService,
    ).orNotFound

    for {
      _ <- logger.info(s"Initializing server on $host:$port")
      _ <- logger.info(s"GraphQL endpoint will be available at http://$host:$port/graphql")
      _ <- logger.info(s"GraphiQL interface will be available at http://$host:$port/graphiql")

      server <- EmberServerBuilder
                  .default[IO]
                  .withHost(host)
                  .withPort(port)
                  .withHttpApp(app)
                  .build
                  .use { server =>
                    logger.info(s"Server started successfully on port $port") >>
                      logger.info("Press Ctrl+C to stop the server...") >>
                      IO.never
                  }
                  .handleErrorWith { error =>
                    logger.error(s"Failed to start server: ${error.getMessage}") >>
                      logger.error(s"Error details: ${error.getStackTrace.mkString("\n")}") >>
                      IO.raiseError(error)
                  }
    } yield ExitCode.Success
  }
}
