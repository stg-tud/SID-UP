package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.InTxn

abstract class SingleDependentReactive(tx: InTxn) {
  self: DependentReactive[_]=>

  protected val dependency: Reactive.Dependency
  dependency.addDependant(tx, this)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
  }

  protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependency.sourceDependencies(tx)
  }
}