package reactive
package impl

import java.util.UUID

trait SingleDependentReactive[P] extends DependentReactive[P] {
  self: ReactiveImpl[_, _, P] =>

  protected val dependency: Reactive[_, _, _]
  dependency.addDependant(null, this)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
  }

  protected def reevaluateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependency.sourceDependencies(transaction)
  }
}