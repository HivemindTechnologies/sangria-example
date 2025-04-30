package com.hivemind.schema

import sangria.schema.*
import sangria.macros.derive.*
import com.hivemind.models.Book

object SchemaDefinition {
  implicit val BookType: ObjectType[Unit, Book] = deriveObjectType[Unit, Book](
    ObjectTypeDescription("A book in the library"),
  )

  val QueryType = ObjectType(
    "Query",
    fields[Unit, Unit](
      Field(
        "books",
        ListType(BookType),
        description = Some("Returns a list of all books."),
        resolve = _ =>
          List(
            Book("1", "The Great Gatsby", "F. Scott Fitzgerald", 1925, "Classic"),
            Book("2", "1984", "George Orwell", 1949, "Dystopian"),
            Book("3", "To Kill a Mockingbird", "Harper Lee", 1960, "Fiction"),
          ),
      ),
    ),
  )

  val schema = Schema(QueryType)
}
