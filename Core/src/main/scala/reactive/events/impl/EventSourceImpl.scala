package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl

import scala.concurrent.stm.InTxn

class EventSourceImpl[A] extends EventStreamImpl[A] with ReactiveSourceImpl[A, A] with EventSource[A] {
  self =>
  protected def makePulse(tx: InTxn, value: A): Option[A] = Some(value)
  override object transactional extends {
    override val impl = self
  } with EventStreamImpl.ViewImpl[A] with ReactiveSourceImpl.ViewImpl[A] 
}
