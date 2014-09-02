package reactive
package impl

import java.util.UUID

trait SingleDependentReactive {
  self: DependentReactive[_]=>

  protected val dependency: Reactive.Dependency
  dependency.addDependant(null, this)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependency.sourceDependencies(transaction)
  }
}