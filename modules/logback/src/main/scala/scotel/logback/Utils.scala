package scotel.logback

import java.net.InetAddress
import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter
import ch.qos.logback.classic.spi.IThrowableProxy

object Utils {

  // Logging standard (https://learn.mdsol.com/display/CA/Logging)
  // states that the inner exception should be printed first
  def getStacktraceLines(t: IThrowableProxy): List[String] =
    RootCauseFirstThrowablePrinter.throwableProxyToStackLines(t)

  def getMachineName(component: String): String =
    component + "-" + InetAddress.getLocalHost.getHostName

  private object RootCauseFirstThrowablePrinter
      extends RootCauseFirstThrowableProxyConverter {
    this.start()

    def throwableProxyToStackLines(t: IThrowableProxy): List[String] =
      throwableProxyToString(t)
        .split("\n")
        .map(_.dropWhile(_ == '\t'))
        .toList
  }

}
