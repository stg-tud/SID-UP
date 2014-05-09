package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.Ref
import reactive.Reactive.PulsedState
import scala.concurrent.stm.InTxn

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  override def toString = name

  private val _sourceDependencies = Ref(calculateSourceDependencies(null))
  override def sourceDependencies(tx: InTxn) = _sourceDependencies()(tx)

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean): Unit = {
    val tx = transaction.stmTx
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(tx)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
        val newDepdencies = calculateSourceDependencies(tx)
      _sourceDependencies.swap(newDepdencies)(tx) != newDepdencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse)
  }

  protected def reevaluate(tx: InTxn): Option[P]
  protected def calculateSourceDependencies(tx: InTxn): Set[UUID]

}
