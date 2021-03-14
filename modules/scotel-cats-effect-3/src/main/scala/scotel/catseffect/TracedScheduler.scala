package scotel.catseffect

import cats.effect.unsafe.Scheduler
import io.opentelemetry.context.Context

import scala.concurrent.duration.FiniteDuration

class TracedScheduler(private val underlying: Scheduler) extends Scheduler {
  override def sleep(delay: FiniteDuration, task: Runnable): Runnable =
    underlying.sleep(delay, Context.current().wrap(task))

  override def nowMillis(): Long = underlying.nowMillis()

  override def monotonicNanos(): Long = underlying.monotonicNanos()
}

object TracedScheduler {
  def apply(scheduler: Scheduler): TracedScheduler =
    new TracedScheduler(scheduler)
}
