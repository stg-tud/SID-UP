package reactive

import java.util.UUID
import scala.collection.immutable.TreeMap
import util.TicketAccumulator
import util.TransactionAction
import util.COMMIT

class TransactionBuilder {
//  private val accu = new TicketAccumulator
  
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
    val sourceIds = boxSet.map(_.uuid);
//    new TransactionExecutor[Transaction] {
//      override def newTransactionId = new Transaction(sourceIds);
//    }.retryUntilSuccess { implicit t =>
//      TransactionExecutor.spawnSubtransactions(boxSet) { setBoxFromMap(t, _) }
//    }
    val transaction = new Transaction(sourceIds);
//    var reply : TransactionAction = null
//    accu.initializeForNotification(boxSet.size) { result => accu.synchronized { reply = result; accu.notifyAll(); } };
    boxSet.foreach(setBoxFromMap(/*accu, */transaction, _));
    
//    val start = System.currentTimeMillis();
//    accu.synchronized {
//      val timeout = 10000;
//      var wait = (start - System.currentTimeMillis() + timeout);
//      while(reply == null && wait > 0) {
//        accu.wait(wait);
//        wait = (start - System.currentTimeMillis() + timeout);
//      }
//    }
//    if(reply != COMMIT) { throw new IllegalStateException("Did not receive a transaction action consensus") }
  }

  private def setBoxFromMap[A](/*replyChannel : TicketAccumulator.Receiver, */t: Transaction, box: ReactiveSource[A]) {
    box.emit(t, boxes(box).asInstanceOf[A]/*, replyChannel*/)
  }
}