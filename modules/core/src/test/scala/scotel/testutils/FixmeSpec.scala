package scotel.testutils

import io.opentelemetry.api.trace.Tracer
import DrawSpan._

import scala.concurrent.{ExecutionContext, Future}
import FixmeSpec._
import scotel.FutureSpanUtils.markSpan

class FixmeSpec extends munit.FunSuite {
  val spanExporter = InMemorySpanExporter()
  val (ec, otel) = setupTraceProviderWithNewThreadPool(spanExporter)

  implicit val ctx: ExecutionContext = ec

  val tracer = otel.getTracer("test_tracer")

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
        pprint.pprintln(spanRes)
        println(drawSpanDiagram(Vector(spanRes)))
      }
  }

  override def afterEach(context: AfterEach): Unit = {
    super.afterEach(context)
    spanExporter.reset()
  }

}

object FixmeSpec {

  private def mkGo(tracer: Tracer, series: Int)(
    implicit ec: ExecutionContext,
  ) = {
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
    } yield ()
  }
}
