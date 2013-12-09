package elmish
package impl

import java.util.UUID

trait SingleDependentReactive {
  self: DependentReactive[_, _]=>

  protected val dependency: Reactive[_, _, _]
  dependency.addDependant(null, this)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    synchronized {
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependency.sourceDependencies(transaction)
  }
}