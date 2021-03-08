package scotel.akkahttp

import akka.NotUsed
import akka.actor.{actorRef2Scala, ActorSystem}
import akka.http.scaladsl.model.UriRendering.UriRenderer
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, UriRendering}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import io.opentelemetry.api.trace.{Span, SpanBuilder, Tracer}
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.javaagent.instrumentation.akkahttp.AkkaHttpServerInstrumentationModule

import scala.jdk.CollectionConverters._
import java.lang
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object RouteWrapper {

  def tracedRoute(
    tracer: Tracer,
    ec: ExecutionContext,
    actorSystem: ActorSystem,
  )(
    innerRoute: Route,
  ): HttpRequest => Future[HttpResponse] = {
    new AkkaHttpServerInstrumentationModule.AsyncWrapper(
      Route.toFunction(innerRoute)(actorSystem),
      actorSystem.getDispatcher,
    )

  }

}
