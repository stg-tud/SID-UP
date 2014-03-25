package reactive

import com.typesafe.scalalogging.slf4j.Logging

case class TransactionBuilder(private val boxes: Map[ReactiveSource[_], Any] = Map()) extends Logging {

  def set[A](box: ReactiveSource[A], value: A): TransactionBuilder = copy(boxes = boxes.updated(box, value))

  def commit(): Transaction = {
    val boxSet = boxes.keySet
    val sourceIds = boxSet.map(_.uuid)

    val transaction = new Transaction(sourceIds)

    logger.trace(s"start $transaction")
    boxSet.foreach(setBoxFromMap(transaction, _))
    logger.trace(s"finish $transaction")

    transaction
  }

  private def setBoxFromMap[A](t: Transaction, box: ReactiveSource[A]) =
    box.emit(t, boxes(box).asInstanceOf[A])
}
