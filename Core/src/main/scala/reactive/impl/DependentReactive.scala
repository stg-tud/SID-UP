package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic
import reactive.Reactive.PulsedState

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  override def toString = name

  private var _sourceDependencies = Ref(calculateSourceDependencies(null))
  override def sourceDependencies(transaction: Transaction) = atomic { tx => _sourceDependencies()(tx) }

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean): Unit = atomic { tx =>
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(transaction: Transaction)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
        val newDepdencies = calculateSourceDependencies(transaction)
      _sourceDependencies.swap(newDepdencies)(tx) != newDepdencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse)
  }

  protected def reevaluate(transaction: Transaction): Option[P]
  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID]

}
