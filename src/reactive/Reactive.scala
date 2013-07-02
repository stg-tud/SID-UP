package reactive

import java.util.UUID
import reactive.signals.Signal

trait Reactive[+O, +V, +P] {
  protected[reactive] def sourceDependencies: Set[UUID]
  protected[reactive] def isConnectedTo(transaction: Transaction): Boolean
  protected[reactive] def addDependant(maybeTransaction: Option[Transaction], dependant: ReactiveDependant[P]) : Option[ReactiveNotification[P]]
  protected[reactive] def removeDependant(dependant: ReactiveDependant[P])
  def log: Signal[List[O]]
  def observe(obs: O => Unit)
  def unobserve(obs: O => Unit)
}
