package scotel.testkit

import scotel.TracedExecutionContext

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

/**
  * An ExecutionContext for test, backed by a fixed thread pool with async propagation support
  * When resetExecutionContext is called, the underlying thread pool is shutdown and replaced with a new one
  * (To avoid cross contamination of thread locals)
  */
class ResettableExecutionContext extends ExecutionContext {
  private var executorService: ExecutionContextExecutorService =
    createThreadpool()
  private var inner = new TracedExecutionContext(executorService)

  def resetExecutionContext(): Unit = {
    val _ = executorService.shutdownNow()
    executorService = createThreadpool()
    inner = new TracedExecutionContext(executorService)
  }

  def createThreadpool(): ExecutionContextExecutorService = {
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  }

  override def execute(runnable: Runnable): Unit = inner.execute(runnable)

  override def reportFailure(cause: Throwable): Unit =
    inner.reportFailure(cause)

  def shutdown(): Unit = executorService.shutdown()
}
