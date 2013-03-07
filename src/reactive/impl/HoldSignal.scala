package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID

class HoldSignal[A](override val changes: EventStream[A], initialValue: A, t: Txn) extends SignalImpl[A]("hold(" + changes.name + ")", initialValue) with RemoteReactiveDependantImpl[A] {
  if(t == null) {
    TransactionBuilder.retryUntilSuccess { t => connect(t, changes); }
  } else {
    connect(t, changes);
  }

  override def notify(sourceDependencyDiff: Multiset[UUID], value: Option[A])(implicit t: Txn) {
    notifyDependants(sourceDependencyDiff, value);
  }
}
