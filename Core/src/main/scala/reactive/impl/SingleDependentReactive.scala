package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.InTxn

trait SingleDependentReactive {
  self: DependentReactive[_]=>

  protected val dependency: Reactive.Dependency
  scala.concurrent.stm.atomic { dependency.addDependant(_, this) }

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
  }

  protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependency.sourceDependencies(tx)
  }
}