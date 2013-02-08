package reactive.impl

import scala.collection.immutable.Map
import reactive.EventStream
import reactive.EventStreamDependant
import reactive.Event
import java.util.UUID
import reactive.PropagationData

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean) extends StatelessEventStreamImpl[A]("filtered(" + from.name + ", " + op + ")") with EventStreamDependant[A] {
  from.addDependant(this);
  override def sourceDependencies = from.sourceDependencies;
  override def notifyEvent(propagationData : PropagationData, maybeValue: Option[A]) {
    propagate(propagationData, maybeValue.filter(op));
  }
}