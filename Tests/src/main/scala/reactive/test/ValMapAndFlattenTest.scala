package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Signal
import reactive.signals.Var
import reactive.events.EventSource
import reactive.events.EventStream
import reactive.signals.Val

class ValMapAndFlattenTest extends FunSuite {
  test("signal") {
    val signal: Signal[Int] = Var(123)
    val higherSignal: Signal[Signal[Int]] = new Val(signal)
    val evenHigherSignal: Signal[Signal[Signal[Int]]] = new Val(higherSignal)

    expectResult(signal) { higherSignal.flatten[Signal[Int]] }
    expectResult(higherSignal) { evenHigherSignal.flatten[Signal[Signal[Int]]] }
    expectResult(signal) { evenHigherSignal.map(_.flatten[Signal[Int]]).flatten[Signal[Int]] }
  }
  test("events") {
    val stream: EventStream[Int] = EventSource()
    val higherSignal: Signal[EventStream[Int]] = new Val(stream)
    val evenHigherSignal: Signal[Signal[EventStream[Int]]] = new Val(higherSignal)

    expectResult(stream) { higherSignal.flatten[EventStream[Int]] }
    expectResult(higherSignal) { evenHigherSignal.flatten[Signal[EventStream[Int]]] }
    expectResult(stream) { evenHigherSignal.map(_.flatten[EventStream[Int]]).flatten[EventStream[Int]] }
  }

}