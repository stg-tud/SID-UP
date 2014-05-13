package reactive

import java.util.UUID
import scala.collection.immutable.TreeMap
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm._

class TransactionBuilder extends Logging {
  private var boxes = Map[ReactiveSource[_], Any]()

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

  def commit() = atomic { implicit inTxn =>
    val boxSet = boxes.keySet
    val sourceIds = boxSet.map(_.uuid);
    val transaction = Transaction(sourceIds, inTxn);
    logger.trace(s"start $transaction")
    boxSet.foreach(setBoxFromMap( /*accu, */ transaction, _))
    logger.trace(s"finish $transaction")
  }

  private def setBoxFromMap[A]( /*replyChannel : TicketAccumulator.Receiver, */ t: Transaction, box: ReactiveSource[A]) {
    box.emit(t, boxes(box).asInstanceOf[A] /*, replyChannel*/ )
  }
}
