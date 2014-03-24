package reactive

import java.util.UUID

trait ReactiveSource[A] {
  def <<(value: A)
  protected[reactive] def emit(transaction: Transaction, value: A)
  protected[reactive] val uuid : UUID
}