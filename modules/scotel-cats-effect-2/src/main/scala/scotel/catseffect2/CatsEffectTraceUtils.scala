package scotel.catseffect2

import io.opentelemetry.api.trace.{Span, Tracer}
import cats.effect.{ExitCase, Sync}

object CatsEffectTraceUtils {

  // FIXME: rename
  // FIXME: test cancel and error
  def withSpan[F[_], A](tracer: Tracer, name: String)(io: F[A])(
    implicit F: Sync[F],
  ): F[A] = {
    F.flatMap(
      F.delay {
        val span = tracer
          .spanBuilder(name)
          .startSpan()
        (span, span.makeCurrent())
      },
    ) {
      case (span, scope) =>
        F.guaranteeCase(io) {
          case ExitCase.Completed =>
            F.delay {
              span.end()
              scope.close()
            }
          case ExitCase.Error(e) =>
            F.delay {
              span.recordException(e)
              span.end()
              scope.close()
            }
          case ExitCase.Canceled =>
            F.delay {
              span.addEvent("fiber_cancelled")
              span.end()
              scope.close()
            }
        }
    }
  }

}
