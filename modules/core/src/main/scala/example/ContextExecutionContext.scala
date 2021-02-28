package example

import io.opentelemetry.context.Context

import scala.concurrent.ExecutionContext

/**
  * Wrap an existing ExecutionContext, providing context propagation
  */
class ContextExecutionContext(private val underlying: ExecutionContext)
    extends ExecutionContext {
  override def execute(runnable: Runnable): Unit =
    underlying.execute(Context.current().wrap(runnable))

  override def reportFailure(cause: Throwable): Unit =
    underlying.reportFailure(cause)
}
