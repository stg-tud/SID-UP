package unoptimized
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait MultiDependentReactive extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected val dependencies: Set[Reactive[_, _]]
  
  // Optimization removed.
  
  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = dependencies
}
