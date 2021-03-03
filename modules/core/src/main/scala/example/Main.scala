package example

import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.api.trace.{Span, Tracer, TracerProvider}
import io.opentelemetry.context.{Context, ContextStorage}
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import io.opentelemetry.sdk.trace.export.SpanExporter

import scala.jdk.CollectionConverters._
import java.util

object Main {

  // FIXME: delme
  def main(args: Array[String]): Unit = {
    println("Hello com.example.Empty Project!")

    implicit val ctx = new ContextExecutionContext(ExecutionContext.global)

    val traceProvider = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(new SpanExporter {
        override def `export`(
          spans: util.Collection[SpanData],
        ): CompletableResultCode = {
          spans.asScala.foreach { s =>
            println(
              s"Reporting ${s.getTraceId}  ${s.getSpanId} attr: ${s.getAttributes}  Parent: ${s.getParentSpanId}",
            )
          }
          CompletableResultCode.ofSuccess()
        }

        override def flush(): CompletableResultCode =
          CompletableResultCode.ofSuccess()

        override def shutdown(): CompletableResultCode =
          CompletableResultCode.ofSuccess()
      }))
      .build()

    val openTelemetry = new OpenTelemetry {
      override def getTracerProvider: TracerProvider = traceProvider

      override def getPropagators: ContextPropagators =
        io.opentelemetry.context.propagation.ContextPropagators
          .noop() // Can add other context propagator
    }
    GlobalOpenTelemetry.set(openTelemetry)
    val tracer = traceProvider.get("mytracer")

    go(tracer, 1, () => go(tracer, 6))
    go(tracer, 2)
    go(tracer, 3)
    go(tracer, 4)
    go(tracer, 5)

    Thread.sleep(1000)
  }

  private def printSpan(id: Int): Unit =
    println(s"$id - ${spanInfo()}")

  def spanInfo(): String = {
    val s = Span.current()
    val ctx = s.getSpanContext

    s"${ctx.getTraceId}  ${ctx.getSpanId}  "
  }

  def go(tracer: Tracer, id: Int, body: () => Unit = () => ())(
    implicit ec: ExecutionContext,
  ): Unit = {
    Future {
      val s = tracer.spanBuilder(id.toString).setAttribute("id", id).startSpan()
      printSpan(id)
      s.makeCurrent()
      printSpan(id)
    }.map { _ =>
        body()
        printSpan(id)
      }
      .onComplete { _ =>
        println(s"Completing $id ${spanInfo()}")
        Span.current().end()
      }
  }

}
