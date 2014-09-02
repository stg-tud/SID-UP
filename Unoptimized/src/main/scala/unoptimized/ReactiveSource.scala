package unoptimized

import java.util.UUID
import util.TicketAccumulator

trait ReactiveSource[A] {
  def <<(value: A): Unit
  protected[unoptimized] def emit(transaction: Transaction, value: A/*, replyChannels : TicketAccumulator.Receiver**/): Unit
  protected[unoptimized] val uuid : UUID
}