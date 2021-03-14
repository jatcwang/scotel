package scotel.catseffect2

import cats.effect.{ContextShift, IO, Timer}
import scotel.testkit.OtelSuite
import scotel.catseffect2.CatsEffectTraceUtils._
import cats.implicits._
import scotel.testkit.DrawSpan._

// FIXME: need to wrap Scheduler (timer) & document it..
class CatsEffect2TraceUtilsSpec extends OtelSuite {

  implicit private val munitContextShift: ContextShift[IO] =
    IO.contextShift(tracedExecutionContext)

  implicit private val munitTimer: Timer[IO] =
    IO.timer(tracedExecutionContext)

  private val munitIOTransform: ValueTransform =
    new ValueTransform(
      "IO",
      { case e: IO[_] => e.unsafeToFuture() },
    )

  // FIXME: generator
  test("asdf") {
    (1.to(10))
      .toList
      .parUnorderedTraverse { idx =>
        go(idx)
      }
      .map { _ =>
        println(drawSpanDiagram(mkSpanGraph(spanExporter.spans)))
      }
  }

  private def go(i: Int): IO[Unit] = {
    withSpanName(tracer, s"${i}")(for {
      _ <- withSpanName(tracer, s"$i-1")(IO.shift)
      _ <- IO.shift
      _ <- withSpanName(tracer, s"$i-2")(IO.unit)
      _ <- withSpanName(tracer, s"$i-3")(IO.shift)
    } yield ())
  }

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms ++ List(munitIOTransform)

}
