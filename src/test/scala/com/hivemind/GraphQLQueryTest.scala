package com.hivemind

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import sangria.execution.Executor
import sangria.parser.QueryParser
import sangria.marshalling.circe._
import com.hivemind.schema.SchemaDefinition
import com.hivemind.service.BookService
import io.circe.Json
import scala.concurrent.ExecutionContext.Implicits.global

class GraphQLQueryTest extends AsyncFlatSpec with Matchers {
  val bookService = BookService()

  "GraphQL books query" should "return all books" in {
    val query =
      """
        |query {
        |  books {
        |    id
        |    title
        |    author
        |    year
        |    genre
        |  }
        |}
        |""".stripMargin

    val queryAst = QueryParser.parse(query).get

    Executor
      .execute(
        SchemaDefinition.schema,
        queryAst,
        SchemaDefinition.MyCtx(query = SchemaDefinition.Query(), service = bookService),
      )
      .map { result =>
        val books = result.hcursor.downField("data").downField("books").as[List[Json]].getOrElse(List.empty)

        books should have size 3

        // Verify first book
        val firstBook = books.head
        firstBook.hcursor.downField("id").as[String].getOrElse("") shouldBe "1"
        firstBook.hcursor.downField("title").as[String].getOrElse("") shouldBe "The Great Gatsby"
        firstBook.hcursor.downField("author").as[String].getOrElse("") shouldBe "F. Scott Fitzgerald"
        firstBook.hcursor.downField("year").as[Int].getOrElse(0) shouldBe 1925
        firstBook.hcursor.downField("genre").as[String].getOrElse("") shouldBe "Classic"
      }
  }

  it should "return books with selected fields only" in {
    val query =
      """
        |query {
        |  books {
        |    title
        |    author
        |  }
        |}
        |""".stripMargin

    val queryAst = QueryParser.parse(query).get

    Executor
      .execute(
        SchemaDefinition.schema,
        queryAst,
        SchemaDefinition.MyCtx(query = SchemaDefinition.Query(), service = bookService),
      )
      .map { result =>
        val books = result.hcursor.downField("data").downField("books").as[List[Json]].getOrElse(List.empty)

        books should have size 3

        // Verify first book has only title and author
        val firstBook = books.head
        firstBook.hcursor.downField("title").as[String].getOrElse("") shouldBe "The Great Gatsby"
        firstBook.hcursor.downField("author").as[String].getOrElse("") shouldBe "F. Scott Fitzgerald"

        // Verify other fields are not present
        firstBook.hcursor.downField("id").as[String].toOption shouldBe None
        firstBook.hcursor.downField("year").as[Int].toOption shouldBe None
        firstBook.hcursor.downField("genre").as[String].toOption shouldBe None
      }
  }
}
