package reactive.impl
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.SynchronizedMap
import reactive.Var
import reactive.Transaction
import commit.CommitVote

class VarImpl[A](name: String, initialValue: A) extends SignalImpl[A](name, initialValue) with Var[A] {
  def set(value: A) {
    emit(value);
  }

  override def prepareCommit(transaction: Transaction, commitVote: CommitVote, newValue: A) {
    prepareCommit(transaction, Iterable(commitVote), { _ => newValue });
  }
}