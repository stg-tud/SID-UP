package reactive.events

import reactive.Reactive
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.DynamicDependentReactive
import reactive.signals.Signal
import scala.concurrent.stm.InTxn
import scala.language.higherKinds
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A, C[B] <: TraversableLike[B, C[B]]](events: Signal[C[EventStream[A]]], tx: InTxn)(implicit canBuildFrom: CanBuildFrom[C[_], A, C[A]]) extends DynamicDependentReactive(tx) with DependentEventStreamImpl[C[A]] {
  override protected def reevaluate(tx: InTxn): Option[C[A]] = {
    // get the flat list of pulses, if none changed, we also propagate no change
    val pulses = events.transactional.now(tx).flatMap(_.pulse(tx).asOption)
    if (pulses.isEmpty) None else Some(pulses)
  }

  override protected def dependencies(tx: InTxn): Set[Reactive[_, _]] = events.transactional.now(tx).toSet + events
}
