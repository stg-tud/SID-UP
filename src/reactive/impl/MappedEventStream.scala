package reactive.impl

import scala.collection.immutable.Map
import reactive.EventStream
import reactive.EventStreamDependant
import reactive.Event

class MappedEventStream[A, B](from: EventStream[B], op: B => A) extends StatelessEventStreamImpl[A]("mapped(" + from.name + ", " + op + ")") with EventStreamDependant[B] {
  from.addDependant(this);
  override def sourceDependencies = from.sourceDependencies;
  override def notifyEvent(event: Event, maybeValue: Option[B]) {
    notifyDependants(event, maybeValue.map(op));
  }
}