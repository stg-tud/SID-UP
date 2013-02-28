package reactive.impl

import scala.collection.immutable.Map
import reactive.EventStream
import reactive.EventStreamDependant
import reactive.Transaction

class MappedEventStream[A, B](from: EventStream[B], op: B => A) extends EventStreamImpl[A]("mapped(" + from.name + ", " + op + ")") with EventStreamDependant[B] {
  from.addDependant(this);
  override def sourceDependencies = from.sourceDependencies;
  override def notifyEvent(event: Transaction, maybeValue: Option[B]) {
    propagate(event, maybeValue.map(op));
  }
}