package reactive

import java.util.UUID

trait ReactiveSource[A, N <: ReactiveNotification[A]] extends Reactive[A, N] {
  def <<(value: A)
  protected[reactive] def emit(transaction: Transaction, value: A)
  protected[reactive] val uuid : UUID
}