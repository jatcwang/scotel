package scotel.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import io.circe.Json
import io.circe.syntax._

import java.util.Locale

class CirceLoggingLayout(
  var component: String,
  var componentVersion: String,
) extends MdsolLogLayout {
  override def setComponent(c: String): Unit = this.component = c
  override def setVersion(v: String): Unit = this.componentVersion = v

  override def doLayout(uncastEvent: ILoggingEvent): String = {
    // Have to cheat here with a cast, because logback's whole parameterized LoggingEvent is pointless when it
    // doesn't allow you to create an ILoggingEvent (or subclass) easily yourself
    uncastEvent match {
      case l: CirceLoggingEvent => {
        Json.obj(
          // Note, timestamp must be the first field. See MCC-561291
          "timestamp" -> l.getTimeStamp.asJson,
          "level" -> ((l.getLevel) match {
            case Level.TRACE => "trace"
            case Level.DEBUG => "debug"
            case Level.INFO  => "info"
            case Level.WARN  => "warning"
            case Level.ERROR => "error"
            case level       => level.levelStr.toString.toLowerCase(Locale.ROOT)
          }).asJson,
          "message" -> l.getMessage.asJson,
          "data" -> encodeExtraLogData(l.data),
          "exception" -> l.exception.asJson,
          "stack" -> l.stack.asJson,
          "component" -> l.component.asJson,
          "component_version" -> l.componentVersion.asJson,
          "machine" -> l.machine.asJson,
          "tracing" -> Json.obj(
            "trace_id" -> l.spanInfo.traceId.asJson,
            "span_id" -> l.spanInfo.spanId.asJson,
          ),
        )
      }
      case _ =>
    }

  }

  protected def makeMachineName(): String = Utils.getMachineName(this.component)
}
