package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util
import util.TicketAccumulator
import util.Update

class VarImpl[A](initialValue: A) extends SignalImpl[A] with ReactiveSourceImpl[A, A] with Var[A] {
  private var value = initialValue
  def now = value
  def value(t: Transaction) = value
  protected def makePulse(value: A): A = value
  
  protected[reactive] override def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[A]) {
    value = pulse.get
  }
}