package reactive.events

import reactive.impl.DynamicDependentReactive
import reactive.{Reactive, Transaction}
import reactive.signals.Signal
import reactive.events.impl.DependentEventStreamImpl
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A, C[B] <: Traversable[B]](events: Signal[C[EventStream[A]]])(implicit canBuildFrom: CanBuildFrom[C[_], A, C[A]]) extends DependentEventStreamImpl[C[A]] with DynamicDependentReactive {
  override protected def reevaluate(transaction: Transaction): Option[C[A]] = {
    val list = events.value(transaction)
    val builder = canBuildFrom.apply(list);
    builder.sizeHint(list.size)
    for(event <- list; pulseValue <- event.pulse(transaction)) builder += pulseValue 
    val result = builder.result
    if(result.isEmpty) None else Some(result)
  }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = events.value(transaction).toSet + events
}

