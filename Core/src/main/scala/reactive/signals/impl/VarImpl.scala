package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util
import scala.concurrent.stm._

class VarImpl[A](initialValue: A) extends SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  private val value = Ref(initialValue)
  def now = atomic { value()(_) }
  def value(t: Transaction) = atomic { value()(_) }
  protected def makePulse(transaction: Transaction, newValue: A): Option[A] = {
    atomic { tx =>
      if (value.swap(newValue)(tx) == newValue) {
        None
      } else {
        Some(newValue)
      }
    }
  }
}