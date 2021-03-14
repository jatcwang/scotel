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
        span.makeCurrent()
      }
      .flatMap { scope =>
        // Note: scope.close() will set current context to the parent span context into
        // Parent span context was captured when span.makeCurrent() was called
        F.guaranteeCase(io) { outcome =>
          outcome.fold(
            canceled = F.delay {
              Span.current().end()
              scope.close()
            },
            errored = e =>
              F.delay {
                val span = Span.current()
                span.recordException(e)
                span.end()
                scope.close()
              },
            completed = next =>
              F.delay {
                  Span.current().end()
                  scope.close()
                }
                .flatMap(_ => next)
                .void,
          )
        }
      }
  }

}
