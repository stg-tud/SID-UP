package unoptimized
package impl

import java.util.UUID

trait SingleDependentReactive {
  self: DependentReactive[_]=>

  protected val dependency: Reactive[_, _]
  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = Set(dependency)
}