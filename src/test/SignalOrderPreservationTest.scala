package test

import reactive.Var
import reactive.Reactive
import reactive.Transaction
import scala.collection.mutable
import reactive.Signal
import testtools.MessageMixup
import testtools.Asserts
import reactive.Lift._
import reactive.LiftableWrappers._
import org.scalatest.FunSuite

class SignalOrderPreservationTest extends FunSuite {
  test("observer and depednants preserve order when incoming messages arrive out of order") {

    val input = Var(1);
    val inputLog = input.log

    val screwedUpThroughNetwork = new MessageMixup(input);
    val networkLog = (screwedUpThroughNetwork : Signal[Int]).log;

    val direct = add(input, 1: Signal[Int]);
    val directLog = direct.log

    val output = add(direct, screwedUpThroughNetwork)
    val outputLog = output.log;

    input.set(3);
    input.set(9);
    input.set(1);
    input.set(5);
    input.set(5);
    input.set(2);

    expectResult(List(1, 3, 9, 1, 5, 2)) { inputLog.now }
    expectResult(List(2, 4, 10, 2, 6, 3)) { directLog.now }
    expectResult(List(1)) { networkLog.now }
    expectResult(List(3)) { outputLog.now }

    screwedUpThroughNetwork.releaseQueue;

    expectResult(List(1, 3, 9, 1, 5, 2)) { inputLog.now }
    expectResult(List(2, 4, 10, 2, 6, 3)) { directLog.now }
    expectResult(List(1, 3, 9, 1, 5, 2)) { networkLog.now }
    expectResult(List(3, 7, 19, 3, 11, 5)) { outputLog.now }
  }
}