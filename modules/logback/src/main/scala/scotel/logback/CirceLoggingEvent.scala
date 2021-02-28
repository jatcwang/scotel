package scotel.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import example.SpanInfo
import io.circe.JsonObject

final class CirceLoggingEvent(
  val underlying: ILoggingEvent,
  val spanInfo: SpanInfo,
  val customData: JsonObject,
) extends LogEventWithSpan(underlying, spanInfo)
