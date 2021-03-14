package scotel.catseffect2

import io.opentelemetry.api.trace.{Span, Tracer}
import cats.effect.{ExitCase, Sync}

object CatsEffectTraceUtils {

  // FIXME: test cancel and error
  def withSpanName[F[_], A](tracer: Tracer, name: String)(io: F[A])(
    implicit F: Sync[F],
  ): F[A] = {
    F.flatMap(
      F.delay {
        val span = tracer
          .spanBuilder(name)
          .startSpan()
        span.makeCurrent()
      },
    ) { scope =>
      F.guaranteeCase(io) {
        case ExitCase.Completed =>
          F.delay {
            Span.current().end()
            scope.close()
          }
        case ExitCase.Error(e) =>
          F.delay {
            val span = Span.current()
            span.recordException(e)
            span.end()
            scope.close()
          }
        case ExitCase.Canceled =>
          F.delay {
            val span = Span.current()
            span.addEvent("cancelled")
            span.end()
            scope.close()
          }
      }
    }
  }

}
