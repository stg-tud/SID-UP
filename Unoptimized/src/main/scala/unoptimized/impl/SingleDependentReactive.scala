package unoptimized
package impl

import java.util.UUID

trait SingleDependentReactive {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected val dependency: Reactive[_, _]
  
  // Optimization removed.
  
  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = Set(dependency)
}