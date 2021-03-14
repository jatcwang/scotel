package scotel

import io.opentelemetry.context.Context

import scala.concurrent.ExecutionContext

/**
  * Wrap an existing ExecutionContext, providing context propagation
  */
class TracedExecutionContext(private val underlying: ExecutionContext)
    extends ExecutionContext {
  override def execute(runnable: Runnable): Unit =
    underlying.execute(Context.current().wrap(runnable))

  override def reportFailure(cause: Throwable): Unit =
    underlying.reportFailure(cause)
}

object TracedExecutionContext {
  def apply(ec: ExecutionContext): TracedExecutionContext =
    new TracedExecutionContext(ec)
}
