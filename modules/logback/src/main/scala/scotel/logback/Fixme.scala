package scotel.logback

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.core.AsyncAppenderBase
import io.opentelemetry.api.trace.Span
import org.slf4j.{Logger, MDC, Marker}

object Fixme {
  /*
  - extends AsyncAppenderBase to capture span context + any other context key (circe/spray custom data)
  - console encoder for the specialized event?
 */
}
