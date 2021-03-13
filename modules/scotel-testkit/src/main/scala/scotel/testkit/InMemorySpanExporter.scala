package scotel.testkit

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.`export`.SpanExporter
import io.opentelemetry.sdk.trace.data.SpanData

import java.util
import java.util.concurrent.atomic.AtomicReference
import scala.jdk.CollectionConverters._

/**
  * InMemorySpanExporter
  */
class InMemorySpanExporter extends SpanExporter {
  private val collectedSpans = new AtomicReference(Vector.empty[SpanData])

  def spans: Vector[SpanData] = collectedSpans.get()
  def reset(): Unit = {
    collectedSpans.set(Vector.empty[SpanData])
  }

  override def `export`(
    spans: util.Collection[SpanData],
  ): CompletableResultCode = {
    val sps = spans.asScala
    collectedSpans.updateAndGet(v => v ++ sps)
    CompletableResultCode.ofSuccess()
  }

  override def flush(): CompletableResultCode =
    CompletableResultCode.ofSuccess()

  override def shutdown(): CompletableResultCode =
    CompletableResultCode.ofSuccess()
}

object InMemorySpanExporter {
  def apply(): InMemorySpanExporter = new InMemorySpanExporter
}
