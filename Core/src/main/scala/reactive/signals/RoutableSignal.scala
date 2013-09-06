package reactive;
package signals;

import reactive.events.EventStream
import java.util.UUID
import reactive.impl.RoutableReactive

/**
 * this type basically acts as a reroutable reactive property, that acts like
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
trait RoutableSignal[A] extends Signal[A] with ReactiveSource[Signal[A]]

object RoutableSignal {
  def apply[A](initialValue: Signal[A]): RoutableSignal[A] = new RoutableReactive[A, A, A, Signal[A]](initialValue) with RoutableSignal[A] {
    override def changes: EventStream[A] = _output.changes
    override def map[B](op: A => B): Signal[B] = _output.map(op)
    override def flatMap[B](op: A => Signal[B]): Signal[B] = _output.flatMap(op)
    override def flatten[R <: Reactive[_, _, _, R]](implicit evidence: A <:< R): R = _output.flatten
    override def snapshot(when: EventStream[_]): Signal[A] = _output.snapshot(when)
    override def pulse(when: EventStream[_]): EventStream[A] = _output.pulse(when)
  }
}