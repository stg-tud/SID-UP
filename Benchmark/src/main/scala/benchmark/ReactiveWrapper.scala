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

object PlaygroundWrapper extends ReactiveWrapper[reactive.signals.Signal, reactive.signals.Var] {

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

class ElmSimulationWrapper extends ReactiveWrapper[elmish.signals.Signal, elmish.signals.Var] {

  import elmish.signals.{Signal, Var}

  // typesafety, what?
  var sources = Set[elmish.signals.Var[Any]]()

  def map[I, O](signal: Signal[I])(f: (I) => O): Signal[O] = signal.map(f)

  def awaiter[I](signal: Signal[I]): () => Unit = () => ()

  def setValue[V](source: Var[V])(value: V): Unit = setValues(source -> value)

  def setValues[V](changes: (Var[V], V)*): Unit = {
    val tb = new elmish.TransactionBuilder
    // create fake updates, for all vars, then set actually changed ones
    sources.foreach { source => tb.set(source, source.now) }
    changes.foreach { case (source, v) => tb.set(source, v) }
    tb.commit()
  }

  def getValue[V](sink: Signal[V]): V = sink.now

  def makeVar[V](value: V): Var[V] = {
    val res = elmish.signals.Var(value)
    sources = sources + res.asInstanceOf[Var[Any]]
    res
  }

  def transpose[V](signals: Seq[Signal[V]]): Signal[Seq[V]] = new elmish.signals.impl.FunctionalSignal({
    _ => signals.map(_.now)
  }, signals: _*)

  def combine[V, R](signals: Seq[Signal[V]])(f: Seq[V] => R): Signal[R] = new elmish.signals.impl.FunctionalSignal({
    _ => f(signals.map(_.now))
  }, signals: _*)
}

object ScalaRxWrapper extends ReactiveWrapper[rx.Rx, rx.Var] {

  import rx._

  def map[I, O](signal: Rx[I])(f: (I) => O): Rx[O] = signal.map(f)

  def awaiter[I](signal: Rx[I]): () => Unit = () => ()

  def setValue[V](source: rx.Var[V])(value: V): Unit = source() = value

  def setValues[V](changes: (rx.Var[V], V)*): Unit = changes.foreach { case (source, v) => source() = v }

  def getValue[V](sink: Rx[V]): V = sink()

  def makeVar[V](value: V): rx.Var[V] = rx.Var(value)

  def transpose[V](signals: Seq[Rx[V]]): Rx[Seq[V]] = Rx { signals.map(_()) }

  def combine[V, R](signals: Seq[Rx[V]])(f: (Seq[V]) => R): Rx[R] = Rx { f(signals.map(_()))  }
}

object ScalaRxWrapperParallel extends ReactiveWrapper[rx.Rx, rx.Var] {

  import rx._
  import ExecutionContext.Implicits.global

  implicit val propagator = new Propagator.Parallelizing()

  def map[I, O](signal: Rx[I])(f: (I) => O): Rx[O] = signal.map(f)

  def awaiter[I](signal: Rx[I]): () => Unit = {
    val promise = Promise[Unit]()
    val obs = rx.Obs(signal, skipInitial = true) {
      promise.success(())
    }
    () => {
      Await.ready(promise.future, Duration.apply(10, TimeUnit.SECONDS))
      obs.active = false
    }
  }

  def setValue[V](source: rx.Var[V])(value: V): Unit = source() = value

  def setValues[V](changes: (rx.Var[V], V)*): Unit = changes.foreach { case (source, v) => source() = v }

  def getValue[V](sink: Rx[V]): V = sink()

  def makeVar[V](value: V): rx.Var[V] = rx.Var(value)

  def transpose[V](signals: Seq[Rx[V]]): Rx[Seq[V]] = Rx { signals.map(_()) }

  def combine[V, R](signals: Seq[Rx[V]])(f: (Seq[V]) => R): Rx[R] = Rx { f(signals.map(_()))  }
}

object ScalaReactWrapper {

  class WrappedDomain extends scala.react.Domain {
    val scheduler = new ManualScheduler()
    val engine = new Engine()
  }

  def apply(domain: scala.react.Domain = new WrappedDomain()): ReactiveWrapper[domain.type#Signal, domain.type#Var] = {
    new ReactiveWrapper[domain.type#Signal, domain.type#Var] {
      def map[I, O](signal: domain.type#Signal[I])(f: (I) => O): domain.type#Signal[O] = {
        var result: Option[domain.type#Signal[O]] = None
        domain.schedule {
          result = Some { domain.Strict { f(signal()) } }
        }
        domain.runTurn(())
        result.get
      }

      val observer = new domain.Observing {}

      def awaiter[I](signal: domain.type#Signal[I]): () => Unit = () => ()

      def getValue[V](sink: domain.type#Signal[V]): V = sink.getValue

      def setValue[V](source: domain.type#Var[V])(value: V): Unit = setValues(source -> value)

      def setValues[V](changes: (domain.type#Var[V], V)*): Unit = {
        domain.schedule {
          changes.foreach { case (source, v) => source() = v }
        }
        domain.runTurn(())
      }

      def makeVar[V](value: V): domain.type#Var[V] = domain.Var(value)(domain.owner)

      def transpose[V](signals: Seq[domain.type#Signal[V]]): domain.type#Signal[Seq[V]] = {
        var res: Option[domain.type#Signal[Seq[V]]] = None
        domain.schedule {
          res = Some { domain.Strict { signals.map(_()) } }
        }
        domain.runTurn(())
        res.get
      }

      def combine[V, R](signals: Seq[domain.type#Signal[V]])(f: (Seq[V]) => R): domain.type#Signal[R] = {
        var res: Option[domain.type#Signal[R]] = None
        domain.schedule {
          res = Some { domain.Strict { f(signals.map(_())) } }
        }
        domain.runTurn(())
        res.get
      }
    }
  }
}
