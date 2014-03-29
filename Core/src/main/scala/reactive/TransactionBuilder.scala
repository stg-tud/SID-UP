package reactive

import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm._

case class TransactionBuilder(private val boxes: Map[Reactive.Source[_], Any] = Map()) extends Logging {

  def set[A](box: Reactive.Source[A], value: A): TransactionBuilder = copy(boxes = boxes.updated(box, value))

  def commit(): Transaction = atomic { implicit tx =>
    val boxSet = boxes.keySet
    val idSet = boxSet.map(_.uuid)

    val transaction = new Transaction(idSet)
    val tboxSet = TSet.apply[Reactive[_, _]](boxSet.toSeq: _*)
    transaction.addAwait(tboxSet)

    logger.trace(s"start $transaction")
    atomic { implicit tx =>
      boxSet.foreach(setBoxFromMap(transaction, _))
    }
    logger.trace(s"propagate $transaction")
    transaction.propagate()
    logger.trace(s"finish $transaction")


    transaction
  }

  private def setBoxFromMap[A](t: Transaction, box: Reactive.Source[A]) =
    box.emit(t, boxes(box).asInstanceOf[A])
}
