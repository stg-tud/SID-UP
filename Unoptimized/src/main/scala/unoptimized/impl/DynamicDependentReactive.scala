package unoptimized
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait DynamicDependentReactive extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>
}
