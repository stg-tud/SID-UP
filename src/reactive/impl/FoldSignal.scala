package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A, it: Txn) extends SignalImpl[A]("fold(" + source.name + ")", initialValue) with RemoteReactiveDependantImpl[B] {
  TransactionBuilder.retryUntilSuccessWithLocalTransactionIfNeeded(it) {
    connect(_, source)
  }

  override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[B])(implicit t: Txn) {
    if (maybeValue.isDefined) {
      notifyDependants(sourceDependenciesDiff, maybeValue.map { op(this(), _) });
    } else {
      super.notifyDependants(sourceDependenciesDiff, None);
    }
  }
}

object FoldSignal {
  def apply[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) = {

  }
}
