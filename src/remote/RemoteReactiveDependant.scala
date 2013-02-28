package remote
import commit.Committable
import reactive.Transaction
import commit.CommitVote

trait RemoteReactiveDependant[-A] extends Committable {
  def prepareCommit(event: Transaction, commitVote : CommitVote, value : A);
}