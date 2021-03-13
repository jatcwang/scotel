package scotel.testutils

import io.opentelemetry.api.trace.{SpanId, TraceId}
import io.opentelemetry.sdk.trace.data.SpanData
import munit.Assertions._

object DrawSpan {

  def form(
    allSpans: Vector[SpanData],
    rootSpanId: String = SpanId.getInvalid,
  ): Vector[SpanRes] = {
    var lookupByParentId = allSpans.groupBy(_.getParentSpanId)

    def go(parentId: String): Vector[SpanRes] = {
      val spans = lookupByParentId.getOrElse(parentId, Vector.empty)

      lookupByParentId = lookupByParentId - parentId

      spans.map { s =>
        val cc = go(s.getSpanId)
        SpanRes(Some(s), cc)
      }
    }

    val resultSpans = go(rootSpanId)
      .sortBy(s => s.traceId)

    val orphans = lookupByParentId.values.flatten.toVector

    if (orphans.nonEmpty) {
      fail(
        s"There are orphan spans which aren't connected to the provided root ID ${resultSpans}",
      )
    } else {
      resultSpans
    }
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
            Vector(s"$indentSpaces${s.nameNice}") ++ draw(
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
    assertEquals(drawSpanDiagram(rootSpanRes), expectedStripped)
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
