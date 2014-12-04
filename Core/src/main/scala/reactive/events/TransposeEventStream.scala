package reactive.events

import reactive.impl.DynamicDependentReactive
import reactive.{Reactive, Transaction}
import reactive.signals.Signal
import reactive.events.impl.DependentEventStreamImpl
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.collection.TraversableLike

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A, C[B] <: TraversableLike[B, C[B]]](events: Signal[C[EventStream[A]]])(implicit canBuildFrom: CanBuildFrom[C[_], A, C[A]]) extends DependentEventStreamImpl[C[A]] with DynamicDependentReactive {
  override protected def reevaluate(transaction: Transaction): Option[C[A]] = {
    val pulses = events.value(transaction).flatMap(_.pulse(transaction)) 
    if(pulses.isEmpty) None else Some(pulses)
  }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = events.value(transaction).toSet + events
}

