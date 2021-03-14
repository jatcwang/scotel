package scotel
import cats.effect._
import cats.effect.unsafe.IORuntime

package object catseffect {

  /**
    * Create an IORuntime that propagates trace context across async boundaries.
    * Implementation wraps IORuntime.global
    */
  def createDefaultTracedIORuntime: IORuntime = {
    import IORuntime.global

    val (blocking, blockDown) =
      IORuntime.createDefaultBlockingExecutionContext()

    // Reuse the global runtime when possible
    IORuntime(
      compute = TracedExecutionContext(global.compute),
      blocking = TracedExecutionContext(blocking),
      scheduler = TracedScheduler(global.scheduler),
      shutdown = () => {
        blockDown()
        global.shutdown()
      },
      config = global.config,
    )
  }
}
