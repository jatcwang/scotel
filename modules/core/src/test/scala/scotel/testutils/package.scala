package scotel

import example.ContextExecutionContext
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.trace.`export`.{SimpleSpanProcessor, SpanExporter}

import scala.concurrent.ExecutionContext
import io.opentelemetry.sdk.trace.SdkTracerProvider

import java.util.concurrent.Executors

package object testutils {
  def setupTraceProviderWithNewThreadPool(
    spanExporter: SpanExporter,
  ): (ContextExecutionContext, Tracer) = {
    val ctx = new ContextExecutionContext(
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4)),
    )

    val tracerProvider = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build

    val tracer = tracerProvider.get("for_test")

    (ctx, tracer)
  }
}
