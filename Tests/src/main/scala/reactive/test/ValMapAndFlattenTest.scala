package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Signal
import reactive.signals.Var
import reactive.events.EventSource
import reactive.events.EventStream
import reactive.signals.Val

class ValMapAndFlattenTest extends FunSuite {
  // type interference sadly needs a little help here
  def expect[T](t: T)(tt: => T) {
    expectResult(t)(tt)
  }
  
  test("signal") {
    val signal: Signal[Int] = Var(123)
    val higherSignal: Signal[Signal[Int]] = new Val(signal)
    val evenHigherSignal: Signal[Signal[Signal[Int]]] = new Val(higherSignal)

    expect(signal) { higherSignal.flatten }
    expect(higherSignal) { evenHigherSignal.flatten }
    expect(signal) { evenHigherSignal.map(_.flatten).flatten }
  }
  test("events") {
    val stream: EventStream[Int] = EventSource()
    val higherSignal: Signal[EventStream[Int]] = new Val(stream)
    val evenHigherSignal: Signal[Signal[EventStream[Int]]] = new Val(higherSignal)

    expect(stream) { higherSignal.flatten }
    expect(higherSignal) { evenHigherSignal.flatten }
    expect(stream) { evenHigherSignal.map(_.flatten).flatten }
  }

}