package scotel.std

import io.opentelemetry.api.trace.{Span, Tracer}

import scala.concurrent.{ExecutionContext, Future}

object FutureTraceUtils {

  def markSpan[A](tracer: Tracer, opName: String, f: => Future[A])(
    implicit ec: ExecutionContext,
  ): Future[A] = {
    Future {
      val span = tracer
        .spanBuilder(opName)
        .startSpan()
      span.makeCurrent()
    }.flatMap(scope =>
      f.transform(
        { a =>
          Span.current().end()
          scope.close()
          a
        },
        e => {
          val span = Span.current()
          span.recordException(e)
          span.end()
          scope.close()
          e
        },
      ),
    )(ExecutionContext.parasitic)

  }

}

