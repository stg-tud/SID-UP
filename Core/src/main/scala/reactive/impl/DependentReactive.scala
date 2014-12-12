package reactive
package impl

import java.util.UUID

import scala.concurrent.stm.{ InTxn, Ref }

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  private val _sourceDependencies = Ref(scala.concurrent.stm.atomic { calculateSourceDependencies })
  override def sourceDependencies = _sourceDependencies.single.get

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean): Unit = {
    val tx = transaction.stmTx
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(tx)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
      val newDependencies = calculateSourceDependencies(tx)
      tx.synchronized(_sourceDependencies.swap(newDependencies)(tx)) != newDependencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse)
  }

  protected def reevaluate(tx: InTxn): Option[P]
  protected def calculateSourceDependencies(tx: InTxn): Set[UUID]
}

object DependentReactive {
  trait ViewImpl[P] extends Reactive.View[P] {
    protected def impl: DependentReactive[_]
    override protected[reactive] def sourceDependencies(implicit tx: InTxn) = tx.synchronized{impl._sourceDependencies.get}
  }
}
