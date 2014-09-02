package unoptimized
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait MultiDependentReactive extends LazyLogging {
  self: DependentReactive[_] =>

  protected val dependencies: Set[Reactive[_,_]]
  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = dependencies
}
