package reactive

import scala.collection.immutable.Map

class MappedEventStream[A, B](from: EventStream[B], op: B => A) extends EventStreamImpl[A]("mapped(" + from.name + ", " + op + ")") with ReactiveDependant[B] {
  from.addDependant(this);
  override def sourceDependencies = from.sourceDependencies;
  override def notifyEvent(event: Event) {
    notifyDependants(event);
  }
  override def notifyUpdate(event: Event, value: B) {
    notifyDependants(event, op(value));
  }
}