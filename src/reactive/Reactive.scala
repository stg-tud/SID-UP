package reactive

import java.util.UUID
import reactive.signals.Signal

trait Reactive[+A, +N <: ReactiveNotification[A]] {
  protected[reactive] def sourceDependencies : Set[UUID]
  protected[reactive] def isConnectedTo(transaction : Transaction) : Boolean
  protected[reactive] def addDependant(dependant : ReactiveDependant[N])
  protected[reactive] def removeDependant(dependant : ReactiveDependant[N])
//  def log: Signal[List[A]]
  def observe(obs: A => Unit)
  def unobserve(obs: A => Unit)
}
