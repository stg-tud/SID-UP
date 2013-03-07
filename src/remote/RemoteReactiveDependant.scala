package remote
import reactive.Transaction
import java.util.UUID
import util.Multiset
import dctm.vars.TransactionExecutionContext

@remote trait RemoteReactiveDependant[-A] {
  def notify(sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A])(implicit t : Txn);
}
//@remote trait RemoteSignalDependant[-A] extends RemoteReactiveDependant[(A, Boolean)];
//@remote trait RemoteEventStreamDependant[-A] extends RemoteReactiveDependant[Option[A]];
