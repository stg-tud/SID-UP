package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl

class VarImpl[A](initialValue: A) extends SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  private var value = initialValue

  override def now = value

  override def value(t: Transaction) = t.pulse(this).value.getOrElse(value)

  override protected def makePulse(newValue: A): Pulse[A] = {
    if (value == newValue) {
      Pulse.noChange
    }
    else {
      value = newValue
      Pulse.change(newValue)
    }
  }
}
