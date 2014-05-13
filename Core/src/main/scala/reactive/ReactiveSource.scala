package reactive

import java.util.UUID
import scala.concurrent.stm.InTxn

trait ReactiveSource[A] {
  def <<(value: A)
  def set(value: A)
  protected[reactive] def emit(transaction: Transaction, value: A/*, replyChannels : TicketAccumulator.Receiver**/)
  protected[reactive] val uuid : UUID
}