package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import scala.concurrent.stm._

class VarImpl[A](initialValue: A) extends SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  private val _value = Ref(initialValue)

  override def now = atomic { tx => _value()(tx) }

  override protected def setValue(value: A)(implicit tx: InTxn): Unit = _value() = value

  override protected def makePulse(newValue: A): Pulse[A] = {
    if (_value.single.get == newValue) {
      Pulse.noChange
    }
    else {
      Pulse.change(newValue)
    }
  }

}
