package com.hivemind.schema

import sangria.schema.*
import sangria.macros.derive.*
import com.hivemind.models.Book
import com.hivemind.service.BookService
import zio.*
import scala.concurrent.Future
import com.hivemind.service.ServiceUtils.zioToFuture

object SchemaDefinition {
  implicit val BookType: ObjectType[Unit, Book] = deriveObjectType[Unit, Book](
    ObjectTypeDescription("A book in the library"),
  )

  val QueryType = ObjectType(
    "Query",
    fields[BookService, Unit](
      Field(
        "books",
        ListType(BookType),
        description = Some("Returns a list of all books."),
        resolve = ctx => zioToFuture(ctx.ctx.getBooks),
      ),
    ),
  )

  val schema = Schema(QueryType)
}
