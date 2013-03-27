package reactive
package impl

import java.util.UUID
import scala.collection.mutable
import Reactive._
import util.Multiset

class MergeStream[A](streams: Iterable[EventStream[A]], it: Txn) extends EventStreamImpl[A]("merge(" + streams.map { _.name }.mkString(", ") + ")") with RemoteReactiveDependantImpl[A] {
  TransactionBuilder.retryUntilSuccessWithLocalTransactionIfNeeded(it) { t =>
	  streams.foreach { connect(t, _) }
  }

  private val pending = mutable.Map[Transaction, (Int, Option[A])]()

  override def notify(sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A])(implicit t : Txn) {
    val (pendingNotifications: Int, toEmit: Option[A]) = pending.get(t.tid).getOrElse((sourceDependencies.get(), false))
    if (shouldEmit(event, maybeValue.isDefined)) {
      propagate(event, maybeValue)
    }
  }
  private def shouldEmit(event: Transaction, canEmit: Boolean): Boolean = {
    pending.synchronized {
      if (pendingNotifications == 1) {
        pending -= event;
        !hasEmitted
      } else {
        pending += (event -> ((pendingNotifications - 1, hasEmitted || canEmit)));
        !hasEmitted && canEmit
      }
    }
  }
}