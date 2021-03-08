package scotel.testutils

import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.`export`.SpanExporter
import io.opentelemetry.sdk.trace.data.SpanData
import scala.collection.mutable

import java.util

/**
  * InMemorySpanExporter
  */
class InMemorySpanExporter extends SpanExporter {
  private val collectedSpans = mutable.ArrayBuffer.empty[SpanData]

  def getSpanData: Vector[SpanData] = collectedSpans.toVector
  def reset(): Unit = collectedSpans.clear()

  override def `export`(
    spans: util.Collection[SpanData],
  ): CompletableResultCode = {
    spans.forEach { s =>
      collectedSpans += s
    }
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
