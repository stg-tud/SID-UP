package reactive

import java.util.UUID
import scala.collection.immutable.TreeMap

class TransactionBuilder {
  // use an arbitrary constant ordering to prevent deadlocks by lock acquisition during commits
  private var boxes = new TreeMap[ReactiveSource[_, _], Any]()(new Ordering[ReactiveSource[_, _]] {
    override def compare(a: ReactiveSource[_, _], b: ReactiveSource[_, _]) = a.uuid.compareTo(b.uuid)
  })

  def set[A](box: ReactiveSource[A, _ <: ReactiveNotification[A]], value: A) = {
    boxes += box -> value;
    this
  }

  def forget[A](box: ReactiveSource[A, _]) {
    boxes -= box;
  }

  private def reset() {
    boxes = boxes.empty
  }

  def commit() {
    val boxSet = boxes.keySet
    val sourceIds = boxSet.map(_.uuid);
//    new TransactionExecutor[Transaction] {
//      override def newTransactionId = new Transaction(sourceIds);
//    }.retryUntilSuccess { implicit t =>
//      TransactionExecutor.spawnSubtransactions(boxSet) { setBoxFromMap(t, _) }
//    }
    val transaction = new Transaction(sourceIds);
    boxSet.foreach(setBoxFromMap(transaction, _));
  }

  private def setBoxFromMap[A](t: Transaction, box: ReactiveSource[A, _]) {
    box.emit(t, boxes(box).asInstanceOf[A])
  }
}

//object TransactionBuilder extends TransactionExecutor[Transaction] {
//  private val empty = Set[UUID]()
//  override def newTransactoinId = new Transaction(empty)
//}