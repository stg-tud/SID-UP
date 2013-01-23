package test

import reactive.Var
import reactive.Transaction
import reactive.Signal.autoSignalToValue
import reactive.Signal
import testtools.ReactiveLog
import testtools.MessageMixup

object TransactionWithPartialMessageMixupTest extends App {
  val var1 = Var("1", 1);
  val var2 = Var("2", 2);

  val mixup1 = new MessageMixup(var1);

  val output = Signal(mixup1, var2) {
    mixup1 + var2;
  }

  val outputLog = new ReactiveLog(output);

  val transaction = new Transaction();
  val touch = false;
  
  transaction.set(var1, 3);
  if(touch) transaction.touch(var2);
  transaction.commit();

  transaction.set(var1, 2);
  transaction.set(var2, 4);
  transaction.commit();

//  transaction.set(var1, 5);
//  if(touch) transaction.touch(var2);
//  transaction.commit();
//
//  transaction.set(var1, 1);
//  transaction.set(var2, 1);
//  transaction.commit();
//
//  transaction.set(var1, 6);
//  if(touch) transaction.touch(var2);
//  transaction.commit();

  mixup1.releaseQueue();

//  outputLog.assert(3, 5, 6, 9, 2, 7)
  outputLog.assert(3, 5, 6)
}