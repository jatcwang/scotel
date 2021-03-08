package scotel.akkahttp

import io.opentelemetry.sdk.trace.`export`.SpanExporter
import munit.FunSuite
import scotel.testutils.{
  setupTraceProviderWithNewThreadPool,
  InMemorySpanExporter,
}
import RouteWrapper.tracedRoute
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class RouteWrapperSpec
    extends AnyWordSpec
    with ScalatestRouteTest
    with BeforeAndAfterEach {

  val spanExporter: InMemorySpanExporter = InMemorySpanExporter()
  val (ec, tracer) = setupTraceProviderWithNewThreadPool(spanExporter)

  "Records and finishes span for a route" in {}
  override def afterEach(): Unit = {
    spanExporter.reset()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  lazy val route = tracedRoute(tracer = tracer, ec = ec, actorSystem = system) {
    import akka.http.scaladsl.server.Directives._

    get {
      path("hello" / "world") {
        onSuccess(
          Future { 1 + 1 },
        )(_ => complete(StatusCodes.OK))
      }
    }
  }

  override def createActorSystem(): ActorSystem = {
    ActorSystem("RouteWrapperSpec", defaultExecutionContext = Some(ec))
  }

}
