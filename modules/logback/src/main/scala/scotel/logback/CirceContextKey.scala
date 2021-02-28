package scotel.logback

import io.circe.JsonObject
import io.opentelemetry.context.ContextKey

import java.util.function.Supplier

object CirceContextKey {
  val key: ContextKey[JsonObject] =
    ContextKey.named[JsonObject]("circe_custom_data")
}
