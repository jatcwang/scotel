package scotel.testkit

import io.opentelemetry.api.common.{AttributeKey, Attributes}
import io.opentelemetry.api.trace.{Span, SpanId, TraceId}
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import munit.Assertions._

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object DrawSpan {

  private val trimRightPat = """"\s+$"""".r

  private def trimRight(str: String) = {
    trimRightPat.replaceFirstIn(str, "")
  }

  def mkSpanGraph(
    allSpans: Vector[SpanData],
    rootSpanId: String = SpanId.getInvalid,
  ): Vector[SpanRes] = {
    var lookupByParentId = allSpans.groupBy(_.getParentSpanId)

    def go(parentId: String): Vector[SpanRes] = {
      val spans = lookupByParentId.getOrElse(parentId, Vector.empty)

      lookupByParentId = lookupByParentId - parentId

      spans
        .map { s =>
          val cc = go(s.getSpanId)
          SpanRes(Some(s), cc)
        }
        .sortBy(_.nameNice)
    }

    val resultSpans = go(rootSpanId)
      .sortBy(s => s.nameNice)

    val orphans = lookupByParentId.values.flatten.toVector

    if (orphans.nonEmpty) {
      fail(
        s"There are orphan spans which aren't connected to the provided root ID ${resultSpans}",
      )
    } else {
      resultSpans
    }
  }

  implicit class OTelAttributesExtensions(val attrs: Attributes)
      extends AnyVal {
    def getStrAttribute(key: String): Option[String] =
      Option(attrs.get(AttributeKey.stringKey(key)))
  }

  val resolveSpanErrorMsg: SpanRes => List[String] = s => {
    s.data.toList
      .flatMap(
        _.getEvents.asScala
          .filter(ev => ev.getName == SemanticAttributes.EXCEPTION_EVENT_NAME),
      )
      .flatMap(ev =>
        ev.getAttributes
          .getStrAttribute(SemanticAttributes.EXCEPTION_MESSAGE.getKey),
      )
      .map(msg => s"err:$msg")
  }

  val defaultSpanPrinters: SpanRes => List[String] = s => {
    List(
      resolveSpanErrorMsg,
    ).flatMap(_.apply(s))
  }

  /**
    * Extract some message from a span
    */
  trait SpanMsgExtractor {
    def getMsg(span: SpanRes): List[String]
  }

  def drawSpanDiagram(
    allSpanRes: Vector[SpanRes],
    extraPrint: SpanRes => List[String] = defaultSpanPrinters,
  ): String = {
    def draw(
      indentation: Int,
      spanRes: Vector[SpanRes],
    ): Vector[String] = {
      val traceNameLookup = mutable.Map.empty[String, String]
      if (spanRes.isEmpty) Vector.empty
      else {
        val indentSpaces = "  " * indentation
        spanRes
          .flatMap { s =>
            val extras = {
              val traceIdStr = if (indentation == 0) {
                val traceId = s.traceId
                if (traceId == TraceId.getInvalid) {
                  "t:none"
                } else {
                  traceNameLookup.get(traceId) match {
                    case Some(traceName) => traceName
                    case None => {
                      val traceNameFromAttr = s.getStrAttribute("traceName")
                      val traceName = traceNameFromAttr.getOrElse(
                        s"no_trace_name_set",
                      )
                      traceNameLookup += (traceId -> traceName)
                      s"t:${traceName}"
                    }
                  }
                }
              } else ""

              val all =
                (traceIdStr +: extraPrint(s)).filter(_.nonEmpty).mkString(",")
              if (all.nonEmpty) s"[$all]" else ""
            }
            Vector(s"$indentSpaces${s.nameNice} ${extras}") ++ draw(
              indentation + 1,
              s.children,
            )
          }
      }
    }

    draw(0, allSpanRes).map(_.stripTrailing()).mkString("\n")
  }

  def assertSpanDiagram(
    allSpans: Vector[SpanData],
    expectedDiagram: String,
  ): Unit = {
    val expectedStripped = expectedDiagram.stripMargin.linesIterator
      .map(_.stripTrailing())
      .mkString("\n")
      .strip
    val rootSpanRes = mkSpanGraph(allSpans)
    val actual = drawSpanDiagram(rootSpanRes).strip
    assertEquals(actual, expectedStripped)
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
