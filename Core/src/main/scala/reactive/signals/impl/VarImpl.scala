package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl

import scala.concurrent.stm._

class VarImpl[A](initialValue: A) extends {
  override protected val value = Ref(initialValue)
} with SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  self =>
  protected def makePulse(tx: InTxn, newValue: A): Option[A] = {
    updateValue(tx, newValue)
  }
  override object transactional extends {
    override val impl = self
  } with SignalImpl.ViewImpl[A] with ReactiveSourceImpl.ViewImpl[A] 
}
