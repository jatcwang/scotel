package example

final case class SpanInfo(
  traceId: String,
  spanId: String,
  // Not easy to get parent span ID unless we cheat
)
