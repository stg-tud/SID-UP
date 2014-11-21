package paperversion

import reactive.signals.Signal
import reactive.ReactiveSource
import reactive.TransactionBuilder

import scala.concurrent.stm._

object DependentUpdate {
  trait Admissions {
    def +=[T](sourceValuePair: (ReactiveSource[T], T)): Unit = +=(sourceValuePair._1, sourceValuePair._2)
    def +=[T](source: ReactiveSource[T], value: T): Unit
    def -=[T](source: ReactiveSource[T]): Unit
  }
  def apply[S, T](signal: Signal[S])(op: (Admissions, S) => T): T = {
    atomic { tx =>
      val builder = new TransactionBuilder
      object admissions extends Admissions {
        override def +=[T](source: ReactiveSource[T], value: T): Unit = {
          builder.set(source, value)
        }
        override def -=[T](source: ReactiveSource[T]): Unit = {
          builder.forget(source)
        }
      }
      val result = op(admissions, signal.now(tx))
      builder.commit()
      result
    }
  }
}