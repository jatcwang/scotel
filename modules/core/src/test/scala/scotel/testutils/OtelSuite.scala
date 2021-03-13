package scotel.testutils

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import munit.FunSuite

import scala.concurrent.ExecutionContext

class OtelSuite extends FunSuite {

  private val ec: ResettableExecutionContext = new ResettableExecutionContext

  protected val spanExporter: InMemorySpanExporter = InMemorySpanExporter()
  protected val otel: OpenTelemetry = setupTestOtel(spanExporter)
  implicit protected val tracedExecutionContext: ExecutionContext = ec
  protected val tracer: Tracer = otel.getTracer("test_tracer")

  override def afterEach(context: AfterEach): Unit = {
    val _ = context
    ec.resetExecutionContext() // Create a new thread pool
    spanExporter.reset()
    super.afterEach(context)
  }

  override def afterAll(): Unit = {
    ec.shutdown()
    super.afterAll()
  }

}
