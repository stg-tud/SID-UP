package reactive

import java.util.UUID

trait ReactiveSource[A] {
  def <<(value: A): Transaction
  protected[reactive] def emit(transaction: Transaction, value: A): Unit
  protected[reactive] val uuid : UUID
}
