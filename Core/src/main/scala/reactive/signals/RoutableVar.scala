package reactive
package signals

import java.util.UUID
import reactive.events.EventStream
import scala.concurrent.stm.InTxn
import reactive.impl.ReactiveImpl
import reactive.impl.ReactiveInstanceNameMutable
import scala.concurrent.stm.atomic
import scala.collection.generic.CanBuildFrom
import scala.collection.TraversableLike
import scala.language.higherKinds 

/* this type basically acts as a reroutable reactive property, that acts like
 * a regular Var[A] but can also be set to reflect another Signal[A]. For
 * instance, you can set
 * <ul>
 * <li><code>new ReactiveButton.enabled << true</code></li>
 * <li><code>new ReactiveButton.enabled << false</code></li>
 * </ul>
 * but you can also create a direct connection to varying values, such as
 * <ul>
 * <li><code>new ReactiveButton.enabled << new ReactiveCheckbox.selected</code></li>
 * </ul>
 */
trait RoutableVar[A] extends Signal[A] with ReactiveSource[Signal[A]]

object RoutableVar {
  def apply[A](initialValue: Signal[A]): RoutableVar[A] = atomic { transactional(initialValue)(_) }
  object transactional {
    def apply[A](initialValue: Signal[A])(implicit constructionTransaction: InTxn): RoutableVar[A] = new RoutableVar[A] with ReactiveInstanceNameMutable {
      // a Var[Signal[A]] with delegates of all ReactiveSource[Signal[A]] input methods
      val _input = Var(initialValue)

      override def <<(value: Signal[A]) = _input.<<(value)
      override def set(value: Signal[A]) = _input.set(value)
      override protected[reactive] def emit(transaction: Transaction, value: Signal[A] /*, replyChannels: TicketAccumulator.Receiver**/ ) = _input.emit(transaction, value /*, replyChannels: _**/ )
      override protected[reactive] val uuid: UUID = _input.uuid

      // the flattened Signal[A] with delegates of all Signal[A] output methods
      val _output = _input.transactional.flatten
      protected[reactive] override def pulse(tx: InTxn): Reactive.PulsedState[A] = _output.pulse(tx)
      protected[reactive] override def hasPulsed(tx: InTxn): Boolean = _output.hasPulsed(tx)
      protected[reactive] override def sourceDependencies: Set[UUID] = _output.sourceDependencies
      protected[reactive] override def isConnectedTo(transaction: Transaction): Boolean = _output.isConnectedTo(transaction)

      protected[reactive] override def addDependant(tx: InTxn, dependant: Reactive.Dependant) = _output.addDependant(tx, dependant)
      protected[reactive] override def removeDependant(tx: InTxn, dependant: Reactive.Dependant) = _output.removeDependant(tx, dependant)
      override def log: Signal[Seq[A]] = _output.log
      override def observe(obs: A => Unit) = _output.observe(obs)
      override def unobserve(obs: A => Unit) = _output.unobserve(obs)
      override def now: A = _output.now
      override val transactional = _output.transactional
      //    override def apply()(implicit t: Transaction) = _output.apply()
      //    override def transientPulse(t: Transaction) = _output.transientPulse(t)
      override def changes: EventStream[A] = _output.changes
      override def delta: EventStream[(A, A)] = _output.delta
      override def map[B](op: A => B): Signal[B] = _output.map(op)
      override def tmap[B](op: (A, InTxn) => B): Signal[B] = _output.tmap(op)
      override def flatMap[B](op: A => Signal[B]): Signal[B] = _output.flatMap(op)
      override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = _output.flatten
      override def snapshot(when: EventStream[_]): Signal[A] = _output.snapshot(when)
      override def pulse(when: EventStream[_]): EventStream[A] = _output.pulse(when)
      override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): Signal[C[T]] = _output.transposeS
      override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]] = _output.transposeE
    }
  }
}
