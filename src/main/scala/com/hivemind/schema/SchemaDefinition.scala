package com.hivemind.schema

import sangria.schema.*
import sangria.macros.derive.*
import com.hivemind.models.Book
import com.hivemind.service.BookService
import zio.*
import scala.concurrent.Future
import com.hivemind.service.ServiceUtils.zioToFuture
import scala.concurrent.duration.Duration
import scala.concurrent.Await

object SchemaDefinition {
  case class Query() {
    @GraphQLField
    def books(sangria: Context[MyCtx, Unit]): List[Book] = {
      val service = sangria.ctx.service
      val zioTask = service.getBooks
      Await.result(zioToFuture(zioTask), Duration.Inf)
    }
  }

  case class MyCtx(query: Query, service: BookService)

  val QueryType = deriveContextObjectType[MyCtx, Query, Unit](_.query)

  val schema = Schema(QueryType)
}
