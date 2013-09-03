package reactive
package impl

import java.util.UUID

trait DependentReactive[V, P] extends Reactive.Dependant {
  self: ReactiveImpl[_, V, P, _] =>

  override def toString = name

  private var _sourceDependencies = calculateSourceDependencies(null)
  override def sourceDependencies(transaction: Transaction) = _sourceDependencies
  
  protected def reevaluateValue(transaction: Transaction): V
  private var _value = reevaluateValue(null)
  override def now = _value
  override def value(transaction: Transaction) = _value

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean) {
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(transaction: Transaction).map{ case (value, pulse) =>
      	_value = value
      	pulse
      }
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

    doPulse(transaction, sourceDependenciesChanged, pulse);
  }

  protected def reevaluate(transaction: Transaction): Option[(V, P)]
  protected def calculateSourceDependencies(transaction: Transaction): Reactive.Topology

}
