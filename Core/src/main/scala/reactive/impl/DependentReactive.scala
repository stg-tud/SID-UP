package reactive
package impl

import java.util.UUID

trait DependentReactive[V, P] extends Reactive.Dependant {
  self: ReactiveImpl[_, V, P] =>

  override def toString = name

  private var _sourceDependencies = calculateSourceDependencies(null)
  override def sourceDependencies(transaction: Transaction) = _sourceDependencies

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean) {
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(transaction: Transaction)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
      val oldSourceDependencies = _sourceDependencies
      _sourceDependencies = calculateSourceDependencies(transaction)
      oldSourceDependencies != _sourceDependencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse)
  }

  protected def reevaluate(transaction: Transaction): Option[P]
  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID]

}
