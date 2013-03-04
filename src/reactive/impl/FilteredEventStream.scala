package reactive.impl

import reactive.EventStream
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CommitVote
import util.Multiset
import java.util.UUID

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean) extends EventStreamImpl[A]("filtered(" + from.name + ", " + op + ")") with RemoteReactiveDependant[A] {
  from.addDependant(this);
 
  override def notify(transaction: Transaction, commitVote : CommitVote[Transaction], sourceDependencyChange : Multiset[UUID], maybeValue: Option[A]) {
    notifyDependants(transaction, commitVote, sourceDependencyChange, maybeValue.filter(op))
  }
}