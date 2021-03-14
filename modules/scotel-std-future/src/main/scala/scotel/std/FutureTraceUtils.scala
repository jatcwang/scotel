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
      (span, span.makeCurrent())
    }.flatMap {
      case (span, scope) =>
        f.transform(
          { a =>
            span.end()
            scope.close()
            a
          },
          e => {
            span.recordException(e)
            span.end()
            scope.close()
            e
          },
        )
    }(ExecutionContext.parasitic)

  }

}
