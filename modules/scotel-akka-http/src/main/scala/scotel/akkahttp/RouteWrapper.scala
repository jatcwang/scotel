package scotel.akkahttp

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpHeader
import akka.http.scaladsl.model.UriRendering.UriRenderer
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, UriRendering}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.{
  Span,
  SpanBuilder,
  SpanKind,
  StatusCode,
  Tracer,
}
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.instrumentation.api.tracer.ServerSpan
import io.opentelemetry.javaagent.instrumentation.akkahttp.{
  AkkaHttpServerHeaders,
  AkkaHttpServerInstrumentationModule,
}
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes

import scala.jdk.CollectionConverters._
import scala.jdk.CollectionConverters._
import java.lang
import java.util.Optional
import java.util.stream.{Collectors, StreamSupport}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Using}

object RouteWrapper {
  def tracedRoute(
    openTelemetry: OpenTelemetry,
    ec: ExecutionContext, // FIXME: need this?
    actorSystem: ActorSystem,
  )(
    innerRoute: Route,
  ): HttpRequest => Future[HttpResponse] = {
    new Wrappy(
      Route.toFunction(innerRoute)(actorSystem),
      ec,
      openTelemetry,
    )
  }

  // FIXME: rename
  // FIXME: what about selection
  final class Wrappy(
    innerHandler: HttpRequest => Future[HttpResponse],
    ec: ExecutionContext,
    openTel: OpenTelemetry,
  ) extends Function1[HttpRequest, Future[HttpResponse]] {
    private val tracer = openTel.getTracer("scotel.akkahttp") // FIXME: version?
    private val propagators = openTel.getPropagators

    override def apply(httpRequest: HttpRequest): Future[HttpResponse] = {
      val parentContext = propagators.getTextMapPropagator.extract(
        Context.root(),
        httpRequest,
        httpRequestContextGetter,
      )

      val spanBuilder = tracer
        .spanBuilder("akka.request")
        .setSpanKind(SpanKind.SERVER)
        .setParent(parentContext)
        // FIXME: allow customizing what you care about
        .setAttribute(SemanticAttributes.HTTP_METHOD, httpRequest.method.value)
        .setAttribute(SemanticAttributes.HTTP_URL, httpRequest.uri.toString)

      val span = spanBuilder.startSpan()

      // Try with resources, to protect against exception that is immediately thrown
      // when underlying function is called. In any other case, the scope doesn't need to be
      // explicitly closed
      Using(parentContext.`with`(span).makeCurrent()) { _ =>
        innerHandler(httpRequest)
          .transform(
            { httpResponse =>
              val httpStatus = httpResponse.status.intValue()
              span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, httpStatus)
              if (httpStatus >= 100 && httpStatus < 400) {
                span.setStatus(StatusCode.UNSET)
              } else span.setStatus(StatusCode.ERROR)
              span.end()
              httpResponse
            },
            e => {
              endSpanExceptionally(span, e)
              e

            },
          )(ec)
      }.fold(e => {
        // When error occurs immediately when innerHandler is called
        endSpanExceptionally(span, e)
        Future.failed(e)
      }, identity)
    }

    private def endSpanExceptionally(span: Span, e: Throwable): Unit = {
      span.recordException(e)
      span.setStatus(StatusCode.ERROR)
      span.end()
    }
  }

  object AkkaHttpServerHeaders {
    val GETTER = new AkkaHttpServerHeaders
  }

  val httpRequestContextGetter: TextMapGetter[HttpRequest] =
    new TextMapGetter[HttpRequest] {
      override def keys(
        httpRequest: HttpRequest,
      ): java.lang.Iterable[String] = {
        httpRequest.headers.map(_.lowercaseName()).asJava
      }

      override def get(carrier: HttpRequest, key: String): String = {
        val header = carrier.getHeader(key)
        if (header.isPresent) header.get.value
        else null
      }
    }
}
