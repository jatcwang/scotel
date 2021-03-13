package scotel.akkahttp

import scotel.testutils.OtelSuite
import scotel.akkahttp.RouteWrapper.tracedRoute
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.client.RequestBuilding._

import scala.concurrent.Future

class RouteWrapperSpec extends OtelSuite {

  private val actorSystem =
    ActorSystem(
      "RouteWrapperSpec",
      defaultExecutionContext = Some(tracedExecutionContext),
    )

  // FIXME: akka http tests, various error scenarios etc
  test("Records and finishes span for a route") {
    routeFunc(Get("/hello/world")).map { resp =>
      assertEquals(resp.status.intValue, 200)
    }
  }

  override def afterAll(): Unit = {
    // If we shutdown the actor system here it seems to shutdown SBT as well hm
    // But it should be fine, since the underlying thread pool is shutdown
    // actorSystem.terminate()
    super.afterAll()
  }

  lazy val routeFunc =
    tracedRoute(otel, ec = tracedExecutionContext, actorSystem = actorSystem) {
      import akka.http.scaladsl.server.Directives._

      get {
        path("hello" / "world") {
          onSuccess(
            Future { 1 + 1 },
          )(_ => complete(StatusCodes.OK))
        }
      }
    }

}
