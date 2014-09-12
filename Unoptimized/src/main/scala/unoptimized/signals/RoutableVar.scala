package unoptimized
package signals

import unoptimized.events.EventStream
import java.util.UUID

/**
 * this type basically acts as a reroutable unoptimized.property, that acts like
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
  def apply[A](initialValue: Signal[A]): RoutableVar[A] = new RoutableVar[A] {
    // a Var[Signal[A]] with delegates of all ReactiveSource[Signal[A]] input methods
    val _input = Var(initialValue);
    override def <<(value: Signal[A]) = _input.<<(value)
    override protected[unoptimized] def emit(transaction: Transaction, value: Signal[A] /*, replyChannels: TicketAccumulator.Receiver**/ ) = _input.emit(transaction, value /*, replyChannels: _**/ )
    override protected[unoptimized] val uuid: UUID = _input.uuid

    // the flattened Signal[A] with delegates of all Signal[A] output methods
    val _output = _input.flatten
    protected[unoptimized] override def value(transaction: Transaction): A = _output.value(transaction)
    protected[unoptimized] override def pulse(transaction: Transaction): Option[A] = _output.pulse(transaction)
    protected[unoptimized] override def hasPulsed(transaction: Transaction): Boolean = _output.hasPulsed(transaction)
    protected[unoptimized] override def sourceDependencies(transaction: Transaction): Set[UUID] = _output.sourceDependencies(transaction)
    protected[unoptimized] override def sourceDependenciesChanged(transaction: Transaction): Boolean = _output.sourceDependenciesChanged(transaction)
    protected[unoptimized] override def isConnectedTo(transaction: Transaction): Boolean = _output.isConnectedTo(transaction);
    protected[unoptimized] override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.addDependant(transaction, dependant)
    protected[unoptimized] override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.removeDependant(transaction, dependant)
    override def log: Signal[Seq[A]] = _output.log
    override def observe(obs: A => Unit) = _output.observe(obs)
    override def unobserve(obs: A => Unit) = _output.unobserve(obs)
    override def now: A = _output.now
    //    override def apply()(implicit t: Transaction) = _output.apply()
    //    override def transientPulse(t: Transaction) = _output.transientPulse(t)
    override def changes: EventStream[A] = _output.changes
    override def delta: EventStream[(A, A)] = _output.delta
    override def map[B](op: A => B): Signal[B] = _output.map(op)
    override def flatMap[B](op: A => Signal[B]): Signal[B] = _output.flatMap(op)
    override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = _output.flatten
    override def snapshot(when: EventStream[_]): Signal[A] = _output.snapshot(when)
    override def pulse(when: EventStream[_]): EventStream[A] = _output.pulse(when)
  }
}