package reactive

import com.typesafe.scalalogging.LazyLogging
import reactive.impl.ReactiveImpl
import scala.concurrent.stm._
import scala.util.Failure

class TransactionBuilder extends LazyLogging {
  private var boxes = Map[ReactiveSource[_], Any]()

  def set[A](box: ReactiveSource[A], value: A) = {
    boxes += box -> value
    this
  }

  def forget[A](box: ReactiveSource[A]): Unit = {
    boxes -= box
  }

  private def reset(): Unit = {
    boxes = boxes.empty
  }

  def commit() = atomic { implicit inTxn =>
    val boxSet = boxes.keySet
    val sourceIds = boxSet.map(_.uuid)
    val transaction = Transaction(sourceIds, inTxn)
    logger.trace(s"start $transaction")
    ReactiveImpl.parallelForeach(boxSet)(setBoxFromMap(transaction, _)).collect { case Failure(e) => throw e }
    logger.trace(s"finish $transaction")
  }

  private def setBoxFromMap[A](t: Transaction, box: ReactiveSource[A]): Unit = {
    box.emit(t, boxes(box).asInstanceOf[A])
  }
}
