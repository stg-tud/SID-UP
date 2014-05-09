package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util
import scala.concurrent.stm._

class VarImpl[A](initialValue: A) extends SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  override protected val value = Ref(initialValue)
  protected def makePulse(tx: InTxn, newValue: A): Option[A] = {
    updateValue(tx, newValue)
  }
}