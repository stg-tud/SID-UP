package reactive.testtools

import reactive.signals.Signal
import reactive.Transaction
import reactive.impl.SingleDependentReactive
import reactive.signals.impl.DependentSignalImpl
import scala.concurrent.stm.InTxn
import java.util.concurrent.Semaphore


class MessageBuffer[A](val id: String, override val dependency: Signal[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentSignalImpl[A] {

  def log(msg: String): Unit = { /*println(s"[$id @ ${Thread.currentThread().getName()}] $msg")*/ }

  private val delivery = new Semaphore(0)
  private val deliveryConfirmation = new Semaphore(0)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) = {
    log("Received message")
    delivery.acquire()
    log("Delivering message")
    doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    log("Delivered message")
    deliveryConfirmation.release()
  }

  def releaseQueue(): Unit = {
    log("Release queue")
    delivery.release()
    log("Awaiting confirmation")
    deliveryConfirmation.acquire()
    log("Delivery confirmed")
  }

  override protected def reevaluateValue(tx: InTxn): A = dependency.now(tx)
}
