package com.hivemind.service

import zio.*
import scala.concurrent.Future

object ServiceUtils {

  def zioToFuture[A](zio: Task[A]): Future[A] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(zio)(Trace.empty, unsafe)
    }

}
