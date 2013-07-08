package reactive
package impl

import java.util.UUID

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, _, P] =>

  private var _sourceDependencies = reevaluateSourceDependencies(null)
  override def sourceDependencies(transaction: Transaction) = _sourceDependencies

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculatePulse: Boolean) {
    val pulse = if (recalculatePulse) {
      calculatePulse(transaction: Transaction)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
      val oldSourceDependencies = _sourceDependencies
      _sourceDependencies = reevaluateSourceDependencies(transaction)
      oldSourceDependencies != _sourceDependencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse);
  }

  protected def calculatePulse(transaction: Transaction): Option[P]
  protected def reevaluateSourceDependencies(transaction: Transaction): Set[UUID]

}