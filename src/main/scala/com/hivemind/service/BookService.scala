package com.hivemind.service

import zio.*
import com.hivemind.models.Book

class BookService {
  def getBooks: Task[List[Book]] = ZIO.succeed(
    List(
      Book("1", "The Great Gatsby", "F. Scott Fitzgerald", 1925, "Classic"),
      Book("2", "1984", "George Orwell", 1949, "Dystopian"),
      Book("3", "To Kill a Mockingbird", "Harper Lee", 1960, "Fiction"),
    ),
  )
}

object BookService {
  val layer: ZLayer[Any, Nothing, BookService] = ZLayer.succeed(new BookService())
}
