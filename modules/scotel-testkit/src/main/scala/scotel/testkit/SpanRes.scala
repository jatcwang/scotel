package scotel.testkit

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.api.trace.{SpanId, TraceId}

final case class SpanRes(
  data: Option[SpanData],
  children: Vector[SpanRes],
) {
  def nameNice: String = data match {
    case Some(d) => d.getName
    case None    => "none"
  }

  def traceId: String = data match {
    case Some(d) => d.getTraceId
    case None    => TraceId.getInvalid
  }
  def spanId: String = data match {
    case Some(d) => d.getSpanId
    case None    => SpanId.getInvalid
  }
  def parentSpanId: String = data match {
    case Some(d) => d.getParentSpanId
    case None    => SpanId.getInvalid
  }
  def getStrAttribute(key: String): Option[String] = {
    data match {
      case Some(d) => Option(d.getAttributes.get(AttributeKey.stringKey(key)))
      case None    => Some("NOSPANDATA")
    }
  }
}
