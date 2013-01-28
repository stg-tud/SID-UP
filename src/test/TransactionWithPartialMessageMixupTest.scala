package test

import reactive.Var
import reactive.Transaction
import reactive.Signal.autoSignalToValue
import reactive.Signal
import testtools.ReactiveLog
import testtools.MessageMixup
import reactive.Reactive

object TransactionWithPartialMessageMixupTest extends App {
  val var1 = Var("var1", 1);
  val var2 = Var("var2", 2);

  val mixup1 = new MessageMixup(var1);

  val output = Signal(mixup1, var2) {
    mixup1 + var2;
  }

  val outputLog = new ReactiveLog(output);
  // initial value 1+2=3

  val transaction = new Transaction();
  val touch = false;

  Reactive.withThreadPoolSize(4) {
    transaction.set(var1, 3);
    if (touch) transaction.touch(var2);
    transaction.commit();
    // new value 3+2=5

    transaction.set(var1, 2);
    transaction.set(var2, 4);
    transaction.commit();
    // new value 2+4=6

    transaction.set(var1, 5);
    if (touch) transaction.touch(var2);
    transaction.commit();
    // new value 5+4=9

    transaction.set(var1, 1);
    transaction.set(var2, 1);
    transaction.commit();
    // new value 1+1=2

    transaction.set(var1, 6);
    if (touch) transaction.touch(var2);
    val lastEvent = transaction.commit();
    // new value 6+1=7

    Thread.sleep(50);
    mixup1.releaseQueue();
    output.awaitValue(lastEvent);
  }

  Thread.sleep(10);

  outputLog.assert(3, 5, 6, 9, 2, 7)
}