package reactive.impl

import reactive.Transaction
import scala.collection.mutable
import util.ParallelForeach

trait ObservableImpl[O] {
  this: ReactiveImpl[O, _] =>

  private val observers = mutable.Set[O => Unit]()

  def observe(obs: O => Unit): Unit = {
    observers += obs
    logger.trace(s"$this observers: ${observers.size }")
  }

  def unobserve(obs: O => Unit): Unit = {
    observers -= obs
    logger.trace(s"$this observers: ${observers.size }")
  }

  private[reactive] def notifyObservers(transaction: Transaction, value: O): Unit = {
    logger.trace(s"$this -> Observers(${observers.size })")
    ParallelForeach.parallelForeach(observers) { _(value) }
  }

}
