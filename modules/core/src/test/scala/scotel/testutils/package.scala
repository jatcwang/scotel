package scotel

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.`export`.{SimpleSpanProcessor, SpanExporter}
import io.opentelemetry.sdk.trace.SdkTracerProvider

package object testutils {
  def setupTestOtel(
    spanExporter: SpanExporter,
  ): OpenTelemetry = {
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

    openTelemetry
  }

}
