package elmish
package impl

import java.util.UUID

trait DependentReactive[V, P] extends Reactive.Dependant {
  self: ReactiveImpl[_, V, P] =>

  override def toString = name
  
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


    doPulse(transaction, false, pulse);
  }

  protected def reevaluate(transaction: Transaction): Option[(V, P)]

}
