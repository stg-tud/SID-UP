package test

import reactive.Var
import reactive.Reactive
import reactive.DependantReactive
import reactive.Event
import scala.collection.mutable
import reactive.Signal
import reactive.Reactive.autoSignalToValue

object SignalOrderPreservationTest extends App {
  val input = Var(1);
  val inputLog = new ReactiveLog(input);

  val screwedUpThroughNetwork = new MessageMixup(input);
  val networkLog = new ReactiveLog(screwedUpThroughNetwork);

  val direct = Signal(input) {
    input + 1;
  }
  val directLog = new ReactiveLog(direct);

  val output = Signal(direct, screwedUpThroughNetwork) {
    direct + screwedUpThroughNetwork;
  }
  val outputLog = new ReactiveLog(output);

  input.set(3);
  input.set(9);
  input.set(1);
  input.set(5);
  input.set(5);
  input.set(2);

  inputLog.assert(1, 3, 9, 1, 5, 2)
  directLog.assert(2, 4, 10, 2, 6, 3)
  networkLog.assert(1)
  outputLog.assert(3)

  screwedUpThroughNetwork.releaseQueue;

  inputLog.assert(1, 3, 9, 1, 5, 2)
  directLog.assert(2, 4, 10, 2, 6, 3)
  networkLog.assert(1, 2, 5, 1, 9, 3)
  outputLog.assert(3, 7, 19, 3, 11, 5)
}