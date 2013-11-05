package benchmark

import scala.language.higherKinds
import reactive.signals.{Var, Signal}
import rx.Rx

/**
 * this tries to create some common abstractions for reactive implementations
 * to make implementing benchmarks for different frameworks easier
 */
trait ReactiveWrapper[GenSig[_], GenVar[_] <: GenSig[_]] {
  def map[I, O](signal: GenSig[I])(f: I => O): GenSig[O]

  def setValue[V](source: GenVar[V])(value: V): Unit

  def getValue[V](sink: GenSig[V]): V

  def makeVar[V](value: V): GenVar[V]

  def transpose[V](signals: Seq[GenSig[V]]): GenSig[Seq[V]]
}

object PlaygroundWrapper extends ReactiveWrapper[reactive.signals.Signal, reactive.signals.Var] {
  def map[I, O](signal: Signal[I])(f: (I) => O): Signal[O] = signal.map(f)

  def setValue[V](source: Var[V])(value: V): Unit = source << value

  def getValue[V](sink: Signal[V]): V = sink.now

  def makeVar[V](value: V): Var[V] = reactive.signals.Var(value)

  def transpose[V](signals: Seq[Signal[V]]): Signal[Seq[V]] = new reactive.signals.impl.FunctionalSignal({
    _ => signals.map(_.now)
  }, signals: _*)
}

object ScalaRxWrapper extends ReactiveWrapper[rx.Rx, rx.Var] {
  def map[I, O](signal: Rx[I])(f: (I) => O): Rx[O] = signal.map(f)

  def setValue[V](source: rx.Var[V])(value: V): Unit = source() = value

  def getValue[V](sink: Rx[V]): V = sink()

  def makeVar[V](value: V): rx.Var[V] = rx.Var(value)

  def transpose[V](signals: Seq[Rx[V]]): Rx[Seq[V]] = Rx {signals.map(_())}
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
          result = Some{domain.Strict {f(signal())}}
        }
        domain.runTurn(())
        result.get
      }

      def getValue[V](sink: domain.type#Signal[V]): V = sink.getValue

      def setValue[V](source: domain.type#Var[V])(value: V): Unit = {
        domain.schedule(source() = value)
        domain.runTurn(())
      }

      def makeVar[V](value: V): domain.type#Var[V] = domain.Var(value)(domain.owner)

      def transpose[V](signals: Seq[domain.type#Signal[V]]): domain.type#Signal[Seq[V]] = {
        var res: Option[domain.type#Signal[Seq[V]]] = None
        domain.schedule {
          res = Some{domain.Strict { signals.map(_()) }}
        }
        domain.runTurn(())
        res.get
      }
    }
  }
}
