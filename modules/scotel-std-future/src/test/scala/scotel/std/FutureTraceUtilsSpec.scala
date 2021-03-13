package scotel.testkit

import io.opentelemetry.api.trace.Tracer
import DrawSpan._

import scala.concurrent.{ExecutionContext, Future}
import FutureTraceUtilsSpec._
import scotel.std.FutureTraceUtils.markSpan

class FutureTraceUtilsSpec extends OtelSuite {

  test(
    "fixme does not modify span context on the thread where markSpan is called",
  ) {}

  test("asdf") {
    Future
      .sequence(
        List(
          mkGo(tracer, 1),
          mkGo(tracer, 2),
          mkGo(tracer, 3),
          mkGo(tracer, 4),
          mkGo(tracer, 5),
          mkGo(tracer, 6),
          mkGo(tracer, 7),
          mkGo(tracer, 8),
        ),
      )
      .map { _ =>
        val spanRes = form(spanExporter.spans)
        println(drawSpanDiagram(spanRes))
      }
  }

}

object FutureTraceUtilsSpec {

  private def mkGo(tracer: Tracer, series: Int)(
    implicit ec: ExecutionContext,
  ): Future[Unit] = {
    markSpan(
      tracer,
      s"p$series",
      for {
        _ <- markSpan(tracer, s"${series}-1", Future {
          1
        })
        _ <- markSpan(tracer, s"${series}-2", Future {
          2
        })
        _ <- markSpan(tracer, s"${series}-3", Future {
          3
        })
      } yield (),
    )
  }

}
