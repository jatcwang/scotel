package scotel.akkahttp

import akka.http.scaladsl.model.UriRendering.UriRenderer
import akka.http.scaladsl.model.{HttpRequest, UriRendering}
import akka.http.scaladsl.server.{RouteResult, Route}
import akka.http.scaladsl.server.Directives._
import io.opentelemetry.api.trace.{SpanBuilder, Tracer, Span}
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.javaagent.instrumentation.akkahttp.AkkaHttpServerTracer

import scala.jdk.CollectionConverters._
import java.lang
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

object RouteWrapper {
  // FIXME: wrap actor system

  val w3cExtractor = W3CTraceContextPropagator.getInstance()

  private val httpRequestTextMapGetter: TextMapGetter[HttpRequest] =
    new TextMapGetter[HttpRequest] {
      override def keys(carrier: HttpRequest): java.lang.Iterable[String] =
        carrier.headers.map(_.name).asJava
      override def get(carrier: HttpRequest, key: String): String = {
        // assume key is lower-cased already, which is the case when this is only accessed from w3cExtractor.extract
        carrier.headers.collectFirst {
          case h if h.is(key) => h.value
        }.orNull
      }
    }

  def extractOrStartSpan(tracer: Tracer, ec: ExecutionContext)(inner: Route): Route = reqContext => {
     val req  = reqContext.request
      // FIXME: allow override
      val context = w3cExtractor.extract(context, req, httpRequestTextMapGetter)
      val s = tracer
        .spanBuilder(s"HTTP ${req.method.value}")
        .setParent(context)
        .setAttribute("http.method", req.method.value)
        .setAttribute("http.target", s"${req.uri.path.toString}${req.uri.rawQueryString.map(q => s"?$q")}")
        .startSpan()
      inner(reqContext).transform(
        routeResult => routeResult match {
          case RouteResult.Complete(response) => {
            if (response.status.)
          }
          case rr: RouteResult.Rejected => {
            // FIXME: log error
            s.end()
            rr
          }
        },
        e => {
          s.recordException(e)
          s.end()
          e
        }
      )(ec)
    }
  }

}
