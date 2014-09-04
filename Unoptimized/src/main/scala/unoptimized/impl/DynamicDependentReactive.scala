package unoptimized
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait DynamicDependentReactive extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>
  
  // Functionality moved to DependentReactive!
  
  override val isDynamicNode: Boolean = true
}
