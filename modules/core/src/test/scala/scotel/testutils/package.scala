package scotel

import example.ContextExecutionContext
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.`export`.{SimpleSpanProcessor, SpanExporter}

import scala.concurrent.ExecutionContext
import io.opentelemetry.sdk.trace.SdkTracerProvider

import java.util.concurrent.Executors

package object testutils {
  // FIXME: allow shutting down EC
  def setupTraceProviderWithNewThreadPool(
    spanExporter: SpanExporter,
  ): (ContextExecutionContext, OpenTelemetry) = {
    val ctx = new ContextExecutionContext(
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4)),
    )

    val tracerProvider = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
      .build

    val openTelemetry =
      OpenTelemetrySdk
        .builder()
        .setPropagators(
          ContextPropagators.create(W3CTraceContextPropagator.getInstance()),
        )
        .setTracerProvider(tracerProvider)
        .build

    (ctx, openTelemetry)
  }
}
