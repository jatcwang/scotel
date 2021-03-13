package scotel.testutils

import io.opentelemetry.api.trace.{Span, SpanId}
import io.opentelemetry.sdk.trace.data.SpanData
import munit.Assertions._

object DrawSpan {

  final case class SpanRes(
    data: Option[SpanData],
    children: Vector[SpanRes],
  )

  def form(allSpans: Vector[SpanData]) = {

    // FIXME: error out on span with non-root parent (but parent span not found)

    def inner(
      parentSpanData: Option[SpanData],
      spansToSearch: Vector[SpanData],
    ): SpanRes = {
      val id = parentSpanData.map(_.getSpanId).getOrElse(SpanId.getInvalid)
      val (thisChildSpans, otherSpans) =
        spansToSearch.partition(_.getParentSpanId == id)
      val childSpanRes = thisChildSpans.map { s =>
        inner(Some(s), otherSpans)
      }
      SpanRes(
        parentSpanData,
        childSpanRes,
      )
    }

    inner(None, allSpans)
  }

  def drawSpanDiagram(allSpanRes: Vector[SpanRes]): String = {

    def draw(
      indentation: Int,
      unsortedCurSpans: Vector[SpanRes],
    ): Vector[String] = {
      if (unsortedCurSpans.isEmpty) Vector.empty
      else {
        val curSpans =
          unsortedCurSpans.sortBy(s => s.data.map(_.getStartEpochNanos))
        val indentSpaces = "  " * indentation
        curSpans
          .flatMap { s =>
            Vector(s"$indentSpaces${s.data.map(_.getName).getOrElse("ROOT")}") ++ draw(
              indentation + 1,
              s.children,
            )
          }
      }
    }

    draw(0, allSpanRes).mkString("\n")
  }

  def assertSpanDiagram(
    allSpans: Vector[SpanData],
    expectedDiagram: String,
  ): Unit = {
    val expectedStripped = expectedDiagram.stripMargin.strip
    val rootSpanRes = form(allSpans)
    assertEquals(drawSpanDiagram(Vector(rootSpanRes)), expectedStripped)
  }

  def getSpanWithName(
    spans: Seq[SpanData],
    name: String,
  ): SpanData = {
    val s = spans.filter(_.getName == name)
    assertEquals(s.length, 1)
    s.head
  }
}
