package remote
import commit.Committable
import reactive.Transaction
import commit.CommitVote
import java.util.UUID
import util.Multiset

@remote trait RemoteReactiveDependant[-A] {
  def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A]);
}
//@remote trait RemoteSignalDependant[-A] extends RemoteReactiveDependant[(A, Boolean)];
//@remote trait RemoteEventStreamDependant[-A] extends RemoteReactiveDependant[Option[A]];
