package reactive

import java.util.UUID

trait ReactiveSource[A] {
  def <<(value: A): Unit
  protected[reactive] def emit(transaction: Transaction, value: A/*, replyChannels : TicketAccumulator.Receiver**/): Unit
  protected[reactive] val uuid : UUID
}