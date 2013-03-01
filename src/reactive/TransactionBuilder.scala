package reactive

import java.util.UUID
import scala.collection.SortedSet
import scala.collection.immutable.TreeMap
import commit.CountdownAggregateCommitVote
import commit.CommitVote
import commit.Committable

class TransactionBuilder {
  // use an arbitrary constant ordering to prevent deadlocks by lock acquisition during commits
  private var boxes = new TreeMap[ReactiveSource[_], Any]()(new Ordering[ReactiveSource[_]] {
    override def compare(a: ReactiveSource[_], b: ReactiveSource[_]) = a.uuid.compareTo(b.uuid)
  })

  def set[A](box: ReactiveSource[A], value: A) = {
    boxes += box -> value;
    this
  }

  def forget[A](box: ReactiveSource[A]) {
    boxes -= box;
  }

  private def reset() {
    boxes = boxes.empty
  }

  def commit() {
    val boxSet = boxes.keySet
    val transaction = new Transaction(boxSet.map(_.uuid));

    def retryCommit() {
      val commitVote = new CountdownAggregateCommitVote[Transaction](transaction, new CommitVote[Transaction] {
        override def yes(committable : Committable[Transaction]) {
          committable.commit(transaction);
        }
        override def unaffected() {}
        override def no() {
          retryCommit()
        }
      }, boxSet.size);
      Reactive.executePooledForeach(boxSet) { setBoxFromMap(_, transaction, commitVote) }
    }

    retryCommit()
  }

  private def setBoxFromMap[A](box: ReactiveSource[A], transaction: Transaction, commitVote: CommitVote[Transaction]) {
    box.prepareCommit(transaction, commitVote, boxes(box).asInstanceOf[A])
  }
}