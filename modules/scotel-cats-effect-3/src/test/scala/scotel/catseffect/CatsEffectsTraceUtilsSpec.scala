package scotel.catseffect

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import scotel.testkit.OtelSuite
import scotel.catseffect.CatsEffectTraceUtils._
import cats.implicits._
import scotel.testkit.DrawSpan._

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.concurrent.duration._

// FIXME: need to wrap Scheduler (timer) & document it..
class CatsEffectsTraceUtilsSpec extends OtelSuite {

  implicit val ioRuntime: IORuntime = createDefaultTracedIORuntime

  private val nonTracedEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

  private val munitIOTransform: ValueTransform =
    new ValueTransform(
      "IO",
      { case e: IO[_] => e.unsafeToFuture() },
    )

  // FIXME: generator
  test(
    "Fibers running in parallel should not contaminate each other's span context",
  ) {
    (1.to(6))
      .toList
      .map(idx => go(idx))
      .parSequence
      .map { _ =>
        assertSpanDiagram(
          spanExporter.spans,
          """
            |1 [t:1]
            |  1-1 
            |  1-2
            |  1-3
            |2 [t:2]
            |  2-1 
            |  2-2 
            |  2-3 
            |3 [t:3]
            |  3-1 
            |  3-2 
            |  3-3 
            |4 [t:4]
            |  4-1 
            |  4-2 
            |  4-3 
            |5 [t:5]
            |  5-1 
            |  5-2 
            |  5-3 
            |6 [t:6]
            |  6-1 
            |  6-2 
            |  6-3""",
        )
      }
  }

  test("Exception thrown in IOs are recorded") {
    val io = withSpan(tracer, "base", _.setAttribute("traceName", "base"))(
      for {
        _ <- withSpan(tracer, "no_error")(
          IO(1),
        )
        _ <- withSpan(tracer, "span_err_1")(IO {
          throw new Exception("e1")
        }).attempt
        _ <- withSpan(tracer, "span_err_2")(
          IO.raiseError(
            new Exception("e2"),
          ),
        ).attempt
        _ <- withSpan(tracer, "span_err_outer")(
          for {
            _ <- IO.unit
            _ <- withSpan(tracer, "span_err_3") {
              IO.raiseError(
                new Exception("e3"),
              )
            }
          } yield (),
        ).attempt
      } yield (),
    )

    io.map { _ =>
      assertSpanDiagram(
        spanExporter.spans,
        """
          |base [t:base]
          |  no_error
          |  span_err_1 (err:e1)
          |  span_err_2 (err:e2)
          |  span_err_outer (err:e3)
          |    span_err_3 (err:e3)
          """,
      )
    }
  }

  test(
    "Fiber cancellation adds a 'fiber_cancelled' span event and ends the span",
  ) {}

  test(
    "Span does not get lost if execution passes through a non-propagating ExecutionContext",
  ) {
    withSpan(tracer, "base", _.setAttribute("traceName", s"base"))(
      for {
        _ <- withSpan(tracer, "1")(for {
          _ <- IO.sleep(0.millis)
          _ <- IO(()).evalOn(nonTracedEC)
          _ <- withSpan(tracer, "lost_span") {
            IO(())
          }
        } yield ())
        _ <- withSpan(tracer, "2")(for {
          _ <- IO.sleep(0.millis)
          _ <- withSpan(tracer, "yay") {
            IO(())
          }
        } yield ())
      } yield (),
    ).map { _ =>
      assertSpanDiagram(
        spanExporter.spans,
        """
          |base [t:base]
          |  1
          |  2
          |    yay
          |lost_span [t:no_trace_name_set]
          |""".stripMargin,
      )
    }
  }

  private def go(i: Int): IO[Unit] = {
    val sleep = IO.sleep(1.millis)
    withSpan(tracer, s"$i", _.setAttribute("traceName", s"$i"))(for {
      _ <- withSpan(tracer, s"$i-1")(sleep)
      _ <- sleep
      _ <- IO.blocking(())
      _ <- withSpan(tracer, s"$i-2")(sleep)
      _ <- IO.blocking(())
      _ <- withSpan(tracer, s"$i-3")(sleep)
    } yield ())
  }

  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms ++ List(munitIOTransform)

  override def afterAll(): Unit = {
    nonTracedEC.shutdownNow()
    super.afterAll()
  }

}
