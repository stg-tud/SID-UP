package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean, it: Txn) extends EventStreamImpl[A]("filtered(" + from.name + ", " + op + ")") with RemoteReactiveDependantImpl[A] {
  TransactionBuilder.retryUntilSuccessWithLocalTransactionIfNeeded(it) {
	  connect(_, from);
  }
  
  override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
    notifyDependants(sourceDependenciesDiff, maybeValue.filter(op))
  }
}
