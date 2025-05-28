package com.hivemind.models

import sangria.schema.*
import sangria.macros.derive.*
case class Book(
  id: String,
  title: String,
  author: String,
  year: Int,
  genre: String,
)

object Book {
  implicit val BookType: ObjectType[Unit, Book] = deriveObjectType[Unit, Book](
    ObjectTypeDescription("A book in the library"),
  )
}
