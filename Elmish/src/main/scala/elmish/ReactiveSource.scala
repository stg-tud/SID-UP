package elmish

import java.util.UUID
import elmishUtil.TicketAccumulator

trait ReactiveSource[A] {
  def <<(value: A)
  protected[elmish] def emit(transaction: Transaction, value: A/*, replyChannels : TicketAccumulator.Receiver**/)
  protected[elmish] val uuid : UUID
}