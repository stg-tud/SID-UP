package reactive.impl

import scala.collection.immutable.Map
import reactive.EventStream
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CommitVote

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean) extends EventStreamImpl[A]("filtered(" + from.name + ", " + op + ")") with RemoteReactiveDependant[A] {
  from.addDependant(this);
  override def sourceDependencies = from.sourceDependencies;
  override def prepareCommit(transaction: Transaction, commitVote : CommitVote, value: A) {
    if(op(value)) {
      prepareCommit(transaction, Iterable(commitVote), value)
    }
  }
}