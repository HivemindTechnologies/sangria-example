package com.hivemind.service

import zio.*
import scala.concurrent.Future

object ServiceUtils {
  private val runtime = Runtime.default

  def zioToFuture[A](zio: Task[A]): Future[A] =
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.runToFuture(zio)
    }

}
