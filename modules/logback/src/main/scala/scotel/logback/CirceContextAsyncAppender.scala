package scotel.logback

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import example.SpanInfo
import io.circe.JsonObject
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context

class CirceContextAsyncAppender extends AsyncAppender {

  override def append(eventObject: ILoggingEvent): Unit = {
    val circeCustomData = {
      val value = Context.current().get(CirceContextKey.key)
      if (value != null) value else JsonObject.empty
    }
    val spanInfo = {
      val curSpan = Span.current().getSpanContext
      SpanInfo(
        curSpan.getTraceId,
        curSpan.getSpanId,
      )
    }

    super.append(new CirceLoggingEvent(eventObject, spanInfo, circeCustomData))
  }
}
