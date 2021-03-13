package scotel

import io.opentelemetry.api.trace.{Span, Tracer}

import scala.concurrent.{ExecutionContext, Future}

object FutureSpanUtils {

  def markSpan[A](tracer: Tracer, opName: String, f: => Future[A])(
    implicit ec: ExecutionContext,
  ): Future[A] = {
    Future {
      val span = tracer
        .spanBuilder(opName)
        .startSpan()
      span.makeCurrent()
    }.flatMap(_ => f)
      .transform(
        { a =>
          Span.current().end()
          a
        },
        e => {
          val span = Span.current()
          span.recordException(e)
          span.end()
          e
        },
      )
  }

}
