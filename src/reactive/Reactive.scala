package reactive

import java.util.UUID
import reactive.signals.Signal

trait Reactive[+A, +N <: ReactiveNotification[A]] {
  protected[reactive] def sourceDependencies: Set[UUID]
  protected[reactive] def isConnectedTo(transaction: Transaction): Boolean
  protected[reactive] def addDependant(maybeTransaction: Option[Transaction], dependant: ReactiveDependant[N]) : Option[N]
  protected[reactive] def removeDependant(dependant: ReactiveDependant[N])
  def log: Signal[List[A]]
  def observe(obs: A => Unit)
  def unobserve(obs: A => Unit)
}
