package example

import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.api.trace.{Span, Tracer, TracerProvider}
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData

import scala.concurrent.{ExecutionContext, Future}
import io.opentelemetry.sdk.trace.export.SpanExporter

import scala.jdk.CollectionConverters._
import java.util
import scala.util.Random

// FIXME: helper method for batch span processor
object Main {

  // FIXME: delme
  def main(args: Array[String]): Unit = {
    implicit val ctx = new ContextExecutionContext(ExecutionContext.global)

//    GlobalOpenTelemetry.set(openTelemetry)
//    val tracer = traceProvider.get("mytracer")

  }

}
