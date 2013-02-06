package test

import reactive.Var
import reactive.Reactive
import reactive.Event
import scala.collection.mutable
import reactive.Signal
import testtools.MessageMixup
import testtools.Asserts
import reactive.Lift._
import reactive.LiftableWrappers._

object SignalOrderPreservationTest extends App {
  val input = Var(1);
  val inputLog = input.log

  val screwedUpThroughNetwork = new MessageMixup(input);
  val networkLog = screwedUpThroughNetwork.log;

  val direct = add(input, 1 : Signal[Int]);
  val directLog = direct.log

  val output = add(direct, screwedUpThroughNetwork)
  val outputLog = output.log;

  input.set(3);
  input.set(9);
  input.set(1);
  input.set(5);
  input.set(5);
  input.set(2);

  Asserts.assert(List(1, 3, 9, 1, 5, 2), inputLog.now)
  Asserts.assert(List(2, 4, 10, 2, 6, 3), directLog.now)
  Asserts.assert(List(1), networkLog.now)
  Asserts.assert(List(3), outputLog.now)

  screwedUpThroughNetwork.releaseQueue;

  Asserts.assert(List(1, 3, 9, 1, 5, 2), inputLog.now)
  Asserts.assert(List(2, 4, 10, 2, 6, 3), directLog.now)
  Asserts.assert(List(1, 3, 9, 1, 5, 2), networkLog.now)
  Asserts.assert(List(3, 7, 19, 3, 11, 5), outputLog.now)
}