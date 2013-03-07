package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID

class MappedEventStream[A, B](from: EventStream[B], op: B => A, it: Txn) extends EventStreamImpl[A]("filtered(" + from.name + ", " + op + ")") with RemoteReactiveDependantImpl[B] {
  TransactionBuilder.retryUntilSuccessWithLocalTransactionIfNeeded(it) {
    connect(_, from);
  }
  override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[B])(implicit t: Txn) {
    notifyDependants(sourceDependenciesDiff, maybeValue.map(op))
  }
}
