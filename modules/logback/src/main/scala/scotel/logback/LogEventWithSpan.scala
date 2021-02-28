package scotel.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{
  ILoggingEvent,
  IThrowableProxy,
  LoggerContextVO,
}
import example.SpanInfo
import org.slf4j.Marker

import java.util

class LogEventWithSpan(underlying: ILoggingEvent, spanInfo: SpanInfo)
    extends ILoggingEvent {
  override def getThreadName: String = underlying.getThreadName

  override def getLevel: Level = underlying.getLevel

  override def getMessage: String = underlying.getMessage

  override def getArgumentArray: Array[AnyRef] = underlying.getArgumentArray

  override def getFormattedMessage: String = underlying.getFormattedMessage

  override def getLoggerName: String = underlying.getLoggerName

  override def getLoggerContextVO: LoggerContextVO =
    underlying.getLoggerContextVO

  override def getThrowableProxy: IThrowableProxy = underlying.getThrowableProxy

  override def getCallerData: Array[StackTraceElement] =
    underlying.getCallerData

  override def hasCallerData: Boolean = underlying.hasCallerData

  override def getMarker: Marker = underlying.getMarker

  override def getMDCPropertyMap: util.Map[String, String] =
    underlying.getMDCPropertyMap

  override def getMdc: util.Map[String, String] = underlying.getMdc

  override def getTimeStamp: Long = underlying.getTimeStamp

  override def prepareForDeferredProcessing(): Unit =
    underlying.prepareForDeferredProcessing()
}
