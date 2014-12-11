package paperversion

import reactive.signals.Signal
import reactive.ReactiveSource
import reactive.TransactionBuilder
import scala.concurrent.stm._
import reactive.signals.Var
import reactive.ReactiveSource

object DependentUpdate {
  trait ReadsWrites {
    def read[T](signal: Signal[T]): T
    def write[T](source: ReactiveSource[T], value: T): Unit
    def unwrite[T](source: ReactiveSource[T]): Unit
    def +=[T](sourceValuePair: (ReactiveSource[T], T)): Unit = write(sourceValuePair._1, sourceValuePair._2)
    def +=[T](source: ReactiveSource[T], value: T): Unit = write(source, value)
    def -=[T](source: ReactiveSource[T]): Unit = unwrite(source)
  }
  def apply[T](maximumSources: ReactiveSource[_]*)(op: ReadsWrites => T): T = /*apply(maximumSources.toSet, op)
  def apply[T](maximumSources: Set[ReactiveSource[_]])(op: (Admissions, InTxn) => T): T =*/ {
    atomic { tx =>
      val builder = new TransactionBuilder
      object rw extends ReadsWrites {
        override def read[T](signal: Signal[T]): T = {
          signal.now(tx)
        }
        override def write[T](source: ReactiveSource[T], value: T): Unit = {
          assert(maximumSources.contains(source), s"$source not contained in maximumSources")
          builder.set(source, value)
        }
        override def unwrite[T](source: ReactiveSource[T]): Unit = {
          builder.forget(source)
        }
      }
      val result = op(rw)
      builder.commit()
      result
    }
  }
}