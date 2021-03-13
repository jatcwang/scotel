package scotel.akkahttp

import scotel.testutils.{
  setupTraceProviderWithNewThreadPool,
  InMemorySpanExporter,
}
import scotel.akkahttp.RouteWrapper.tracedRoute
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.client.RequestBuilding._
import example.ContextExecutionContext

import scala.concurrent.{ExecutionContext, Future}

class RouteWrapperSpec extends munit.FunSuite {

  val spanExporter: InMemorySpanExporter = InMemorySpanExporter()
  val (ec, openTel) = setupTraceProviderWithNewThreadPool(spanExporter)
  implicit val ectx: ExecutionContext = ec

  val actorSystem =
    ActorSystem("RouteWrapperSpec", defaultExecutionContext = Some(ec))

  test("Records and finishes span for a route") {
    routeFunc(Get("/hello/world")).map { resp =>
      println(spanExporter.spans)
      assertEquals(resp.status.intValue, 200)
    }
  }

  override def afterEach(context: AfterEach): Unit = {
    super.afterEach(context)
  }

  override def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  lazy val routeFunc =
    tracedRoute(openTel, ec = ec, actorSystem = actorSystem) {
      import akka.http.scaladsl.server.Directives._

      get {
        path("hello" / "world") {
          onSuccess(
            Future { 1 + 1 }(ec),
          )(_ => complete(StatusCodes.OK))
        }
      }
    }

}
