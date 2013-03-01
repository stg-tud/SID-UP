package remote
import commit.Committable
import reactive.Transaction
import commit.CommitVote
import java.util.UUID

@remote trait RemoteReactiveDependant[-A] {
  def notify(event: Transaction, commitVote: CommitVote[Transaction], newDependencies : Set[UUID], obsoleteDependencies : Set[UUID], value: Option[A]);
}
//@remote trait RemoteSignalDependant[-A] extends RemoteReactiveDependant[(A, Boolean)];
//@remote trait RemoteEventStreamDependant[-A] extends RemoteReactiveDependant[Option[A]];
