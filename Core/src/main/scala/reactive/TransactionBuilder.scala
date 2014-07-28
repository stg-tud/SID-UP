package reactive

import java.util.UUID
import scala.collection.immutable.TreeMap
import scala.concurrent.stm._
import reactive.impl.ReactiveImpl
import com.typesafe.scalalogging.LazyLogging

class TransactionBuilder extends LazyLogging {
  private var boxes = Map[ReactiveSource[_], Any]()

  def set[A](box: ReactiveSource[A], value: A) = {
    boxes += box -> value;
    this
  }

  def forget[A](box: ReactiveSource[A]): Unit = {
    boxes -= box;
  }

  private def reset(): Unit = {
    boxes = boxes.empty
  }

  def commit() = atomic { implicit inTxn =>
    val boxSet = boxes.keySet
    val sourceIds = boxSet.map(_.uuid);
    val transaction = Transaction(sourceIds, inTxn);
    logger.trace(s"start $transaction")
    ReactiveImpl.parallelForeach(boxSet)(setBoxFromMap( /*accu, */ transaction, _))
    logger.trace(s"finish $transaction")
  }

  private def setBoxFromMap[A]( /*replyChannel : TicketAccumulator.Receiver, */ t: Transaction, box: ReactiveSource[A]): Unit = {
    box.emit(t, boxes(box).asInstanceOf[A] /*, replyChannel*/ )
  }
}
