package paperversion

import reactive.signals.Signal
import reactive.ReactiveSource
import reactive.TransactionBuilder

import scala.concurrent.stm._

object ConditionalUpdate {
  trait Attempt {
    def read[T](signal: Signal[T]): T
    def admit[T](source: ReactiveSource[T], value: T): Unit
  }
  def apply[T](op: Attempt => T): T = {
    atomic { tx =>
      val builder = new TransactionBuilder
      object attempt extends Attempt {
        override def read[T](signal: Signal[T]): T = {
          signal.now(tx)
        }
        override def admit[T](source: ReactiveSource[T], value: T): Unit = {
          builder.set(source, value)
        }
      }
      val result = op(attempt)
      builder.commit()
      result
    }
  }
}