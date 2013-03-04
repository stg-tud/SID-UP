package reactive
import java.util.UUID
import commit.CommitVote
import commit.Committable

trait ReactiveSource[-A] extends Reactive[A] with Committable[Transaction] {
  private val transaction = new TransactionBuilder();
  protected def emit(value: A) = {
    transaction.set(this, value);
    transaction.commit();
  }

  val uuid = UUID.randomUUID();
  def prepareCommit(transaction: Transaction, commitVote : CommitVote[Transaction], value: A)
}