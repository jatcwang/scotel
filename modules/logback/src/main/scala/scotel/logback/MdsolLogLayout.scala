package scotel.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase

trait MdsolLogLayout extends LayoutBase[ILoggingEvent] {
  def setComponent(c: String): Unit
  def setVersion(v: String): Unit
}
