package com.hivemind

import zio.*
import zio.http.*
import zio.logging.*
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import sangria.execution.Executor
import sangria.marshalling.circe.*
import sangria.parser.{QueryParser, SyntaxError}
import sangria.ast.Document
import com.hivemind.schema.SchemaDefinition
import com.hivemind.models.Book
import com.hivemind.service.BookService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import java.io.InputStream
import scala.io.Source
import zio.stream.ZStream
import zio.http.codec.TextBinaryCodec.fromSchema
import scala.io.Source.*

object BookServer extends ZIOAppDefault {
  implicit val bookEncoder: Encoder[Book] = deriveEncoder[Book]
  val bookService                         = new BookService()

  val graphQLRoutes = Routes[Any, Response](
    Method.POST / "graphql"    -> handler { (req: Request) =>
      for {
        body         <- req.body.asString
        json         <- ZIO.fromEither(io.circe.parser.parse(body))
        query         = json.hcursor.downField("query").as[String].getOrElse("")
        variables     = json.hcursor.downField("variables").as[io.circe.Json].getOrElse(io.circe.Json.obj())
        operationName = json.hcursor.downField("operationName").as[String].toOption
        queryAst     <- ZIO.fromTry(QueryParser.parse(query))
        queryResult  <- ZIO.fromFuture { _ =>
                          Executor.execute(
                            SchemaDefinition.schema,
                            queryAst,
                            bookService,
                            variables = variables,
                            operationName = operationName,
                          )
                        }
        response     <- ZIO.succeed(Response.json(queryResult.toString))
      } yield response
    }.mapError(_ => Response.internalServerError),
    Method.OPTIONS / "graphql" -> handler { (_: Request) =>
      ZIO.succeed(Response.ok)
    },
    Method.GET / "graphiql"    -> handler { (_: Request) =>
      for {
        htmlContent <- ZIO.attempt(Source.fromResource("graphiql.html").mkString)
        response     = Response(
                         status = Status.Ok,
                         headers = Headers(Header.ContentType(MediaType.text.html)),
                         body = Body.from(htmlContent),
                       )
      } yield response
    }.mapError(_ => Response.internalServerError),
  )

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Any, Any] = {
    val port = 8081
    val host = "localhost"

    for {
      _ <- ZIO.logInfo(s"Initializing server on $host:$port")
      _ <- ZIO.logInfo(s"GraphQL endpoint will be available at http://$host:$port/graphql")
      _ <- ZIO.logInfo(s"GraphiQL interface will be available at http://$host:$port/graphiql")
      _ <- Server
             .serve[Any](graphQLRoutes)
             .provide(
               Server.defaultWithPort(port),
               Runtime.removeDefaultLoggers,
               Runtime.addLogger(ZLogger.default),
             )
    } yield ()
  }
}
