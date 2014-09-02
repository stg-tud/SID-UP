package util

sealed trait TransactionAction {

  def and(other: TransactionAction): TransactionAction
}

case object COMMIT extends TransactionAction {
  def and(other: TransactionAction) = other;
}
case object ABORT extends TransactionAction {
  def and(other: TransactionAction) = this;
}
case object RETRY extends TransactionAction {
  def and(other: TransactionAction) = if (other == ABORT) other else this
}

class TicketAccumulator extends TicketAccumulator.Receiver {

  private var awaiting: Int = 0
  private var notifyWhenDone: Iterable[TicketAccumulator.Receiver] = _
  private var accumulatedAction: TransactionAction = _

  def initializeForNotification(count: Int)(notifyWhenDone: TicketAccumulator.Receiver*): Unit = {
    this.notifyWhenDone = notifyWhenDone;
    awaiting = count;
    accumulatedAction = COMMIT
    maybeFire();
  }
  override def apply(action: TransactionAction): Unit = {
    if (awaiting == 0) throw new IllegalStateException("Not awaiting any Tickets at this point!");
    awaiting -= 1;
    accumulatedAction = accumulatedAction.and(action)
    maybeFire();
  }

  private def maybeFire(): Unit = {
    if (awaiting == 0) {
      notifyWhenDone.foreach { _(accumulatedAction) }
    }
  }
}

object TicketAccumulator {
  type Receiver = TransactionAction => Unit
}