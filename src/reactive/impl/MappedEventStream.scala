package reactive.impl

import reactive.EventStream
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CommitVote
import util.Multiset
import java.util.UUID

class MappedEventStream[A, B](from: EventStream[B], op: B => A) extends EventStreamImpl[A]("mapped(" + from.name + ", " + op + ")") with RemoteReactiveDependant[B] {
  from.addDependant(this);
  
  override def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependencyDiff : Multiset[UUID], value: Option[B]) {
    notifyDependants(transaction, commitVote, sourceDependencyDiff, value.map(op));
  }
}