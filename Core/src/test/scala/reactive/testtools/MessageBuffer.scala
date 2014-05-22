package reactive.testtools

import reactive.signals.impl.SignalImpl
import reactive.signals.Signal
import scala.collection.mutable
import reactive.Transaction
import reactive.impl.SingleDependentReactive
import reactive.signals.impl.DependentSignalImpl
import scala.concurrent.stm.InTxn

class MessageBuffer[A](val id: String, override val dependency: Signal[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentSignalImpl[A] {
  def log(msg: String) { println("[" + id + " @ " + Thread.currentThread().getName() + "] " + msg) }
  private object parking;
  private var parkingState = 0
  log("Awaiting message")

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    parking.synchronized {
      log("Received message")
      if (parkingState != 0) throw new IllegalStateException
      parkingState = 1
      parking.notifyAll
      while (parkingState == 1) {
        parking.wait
      }
      log("Delivering message")
    }
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    parking.synchronized {
      log("Delivered message")
      while (parkingState != 2) {
        parking.wait
      }
      parkingState = 0
      parking.notifyAll
      log("Awaiting message")
    }
  }

  def releaseQueue() {
    parking.synchronized {
      log("Send command")
      while (parkingState != 1) {
        parking.wait
      }
      parkingState = 2
      parking.notifyAll
      log("Awaiting delivery confirmation")
      while (parkingState == 2) {
        parking.wait
      }
      log("Delivery confirmed.")
    }
  }

  override protected def reevaluateValue(tx: InTxn): A = dependency.now(tx)
}
