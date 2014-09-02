package benchmark

import scala.language.higherKinds
import scala.concurrent.{Await, Promise, ExecutionContext}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * this tries to create some common abstractions for reactive implementations
 * to make implementing benchmarks for different frameworks easier
 */
trait ReactiveWrapper[GenSig[_], GenVar[_] <: GenSig[_]] {
  def map[I, O](signal: GenSig[I])(f: I => O): GenSig[O]

  def awaiter[I](signal: GenSig[I]): () => Unit

  def setValue[V](source: GenVar[V])(value: V): Unit

  def setValues[V](changes: (GenVar[V], V)*): Unit

  def getValue[V](sink: GenSig[V]): V

  def makeVar[V](value: V): GenVar[V]

  def transpose[V](signals: Seq[GenSig[V]]): GenSig[Seq[V]]

  def combine[V, R](signals: Seq[GenSig[V]])(f: Seq[V] => R): GenSig[R]
}

object SidupWrapper extends ReactiveWrapper[reactive.signals.Signal, reactive.signals.Var] {

  import reactive.signals.{Signal, Var}

  def map[I, O](signal: Signal[I])(f: (I) => O): Signal[O] = signal.map(f)

  def awaiter[I](signal: Signal[I]): () => Unit = () => ()

  def setValue[V](source: Var[V])(value: V): Unit = setValues(source -> value)

  def setValues[V](changes: (Var[V], V)*): Unit = {
    val tb = new reactive.TransactionBuilder
    changes.foreach { case (source, v) => tb.set(source, v) }
    tb.commit()
  }

  def getValue[V](sink: Signal[V]): V = sink.now

  def makeVar[V](value: V): Var[V] = reactive.signals.Var(value)

  def transpose[V](signals: Seq[Signal[V]]): Signal[Seq[V]] = new reactive.signals.impl.FunctionalSignal({
    _ => signals.map(_.now)
  }, signals: _*)

  def combine[V, R](signals: Seq[Signal[V]])(f: Seq[V] => R): Signal[R] = new reactive.signals.impl.FunctionalSignal({
    _ => f(signals.map(_.now))
  }, signals: _*)
}

object UnoptimizedWrapper extends ReactiveWrapper[unoptimized.signals.Signal, unoptimized.signals.Var] {

  import unoptimized.signals.{Signal, Var}

  def map[I, O](signal: Signal[I])(f: (I) => O): Signal[O] = signal.map(f)

  def awaiter[I](signal: Signal[I]): () => Unit = () => ()

  def setValue[V](source: Var[V])(value: V): Unit = setValues(source -> value)

  def setValues[V](changes: (Var[V], V)*): Unit = {
    val tb = new unoptimized.TransactionBuilder
    changes.foreach { case (source, v) => tb.set(source, v) }
    tb.commit()
  }

  def getValue[V](sink: Signal[V]): V = sink.now

  def makeVar[V](value: V): Var[V] = unoptimized.signals.Var(value)

  def transpose[V](signals: Seq[Signal[V]]): Signal[Seq[V]] = new unoptimized.signals.impl.FunctionalSignal({
    _ => signals.map(_.now)
  }, signals: _*)

  def combine[V, R](signals: Seq[Signal[V]])(f: Seq[V] => R): Signal[R] = new unoptimized.signals.impl.FunctionalSignal({
    _ => f(signals.map(_.now))
  }, signals: _*)
}

