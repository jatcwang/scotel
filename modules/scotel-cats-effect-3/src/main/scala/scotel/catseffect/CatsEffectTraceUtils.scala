package scotel.catseffect

import io.opentelemetry.api.trace.{Span, SpanBuilder, Tracer}
import cats.effect.Sync
import cats.syntax.all._
import cats.effect.kernel.Outcome

object CatsEffectTraceUtils {

  // FIXME: take implicit tracer
  // FIXME: test cancel and error
  def withSpan[F[_], A](
    tracer: Tracer,
    name: String,
    buildSpan: SpanBuilder => SpanBuilder = identity,
  )(io: F[A])(
    implicit F: Sync[F],
  ): F[A] = {
    F.delay {
        val span = buildSpan(
          tracer
            .spanBuilder(name),
        ).startSpan()
        (span, span.makeCurrent())
      }
      .flatMap {
        case (span, scope) =>
          // We explicitly pass the span through (instead of calling Span.current())
          // to protect against any broken context propagation in `io` (e.g. execution may have
          // passed through an EC doesn't propagate context)
          // scope.close() will set current context to the parent span context,
          // that was captured when span.makeCurrent() was called, so any fibers that continue from here
          // will be siblings of this span, instead of a child.
          F.guaranteeCase(io) { outcome =>
            outcome.fold(
              canceled = F.delay {
                span.addEvent("fiber_cancelled")
                span.end()
                scope.close()
              },
              errored = e =>
                F.delay {
                  span.recordException(e)
                  span.end()
                  scope.close()
                },
              completed = next =>
                F.delay {
                    span.end()
                    scope.close()
                  }
                  .flatMap(_ => next)
                  .void,
            )
          }
      }
  }

}
