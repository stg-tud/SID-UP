package reactive
package impl

import java.util.UUID

import scala.language.higherKinds
trait DependentReactive[X, OW[+_], VW[+_], PW[+_], +R[+Y] <: Reactive[Y, OW, VW, PW, R]] extends Reactive.Dependant {
  self: ReactiveImpl[X, OW, VW, PW, R] =>

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
