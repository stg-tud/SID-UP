package reactive
package impl

import scala.collection.mutable
import com.typesafe.scalalogging.slf4j.Logging
import java.util.concurrent.Executors
import scala.util.Failure
import scala.util.Try
import Reactive._
import scala.concurrent.stm._
import reactive.signals.Signal
import java.util.UUID
import java.lang.reflect.InvocationTargetException
import scala.util.control.ControlThrowable

trait ReactiveImpl[O, P] extends Reactive[O, P] with Logging {
  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction.stmTx)).isEmpty

  private[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)
    s"$unqualifiedClassname($hashCode)"
  }

  override def toString = name

  private val pulse: Ref[PulsedState[P]] = Ref(Pending)
  override def pulse(tx: InTxn): PulsedState[P] = pulse()(tx)
  override def hasPulsed(tx: InTxn): Boolean = pulse(tx).pulsed

  private val dependants = Ref(Set[Reactive.Dependant]())

  override def addDependant(tx: InTxn, dependant: Reactive.Dependant) {
    //    synchronized {
    //      logger.trace(s"$dependant <~ $this [${Option(transaction)}")
    tx.synchronized(dependants.transform(_ + dependant)(tx))
    //    }
  }

  override def removeDependant(tx: InTxn, dependant: Reactive.Dependant) {
    //    synchronized {
    //      logger.trace(s"$dependant <!~ $this [${Option(transaction)}")
    tx.synchronized(dependants.transform(_ - dependant)(tx))
    //    }
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) {
    //    synchronized {
    logger.trace(s"$this => Pulse($pulse, $sourceDependenciesChanged) [${Option(transaction)}")
    transaction.stmTx.synchronized(this.pulse.set(PulsedState(pulse))(transaction.stmTx))
    Txn.beforeCommit(inTxnBeforeCommit => {
      transaction.stmTx.synchronized(this.pulse.set(Pending)(inTxnBeforeCommit))
    })(transaction.stmTx)
    val pulsed = pulse.isDefined

    ReactiveImpl.parallelForeach(transaction.stmTx.synchronized(dependants()(transaction.stmTx))){ dep =>
      dep.ping(transaction, sourceDependenciesChanged, pulsed)
    }
    if (pulsed) {
      val value = getObserverValue(transaction, pulse.get)
      val obsToNotify = transaction.stmTx.synchronized(observers()(transaction.stmTx))
      Txn.afterCommit { _ =>
        ReactiveImpl.parallelForeach(obsToNotify) { _(value) }
      }(transaction.stmTx)
    }
    //    }
  }

  protected def getObserverValue(transaction: Transaction, pulseValue: P): O

  // ====== Observing stuff ======

  private val observers = Ref(Set[O => Unit]())

  def observe(obs: O => Unit)(implicit inTxn: InTxn) {
    val size = inTxn.synchronized(observers.transformAndGet { _ + obs }(inTxn)).size
    logger.trace(s"$this observers: ${size}")
  }

  def unobserve(obs: O => Unit)(implicit inTxn: InTxn) {
    val size = inTxn.synchronized(observers.transformAndGet { _ - obs }(inTxn)).size
    logger.trace(s"$this observers: ${size}")
  }
}

object ReactiveImpl extends Logging {
  trait ViewImpl[O] extends Reactive.View[O] {
    protected val impl: ReactiveImpl[O, _]
    override def log: Signal[Seq[O]] = atomic { impl.log(_) }
    override def observe(obs: O => Unit): Unit = atomic { tx =>
      val size = tx.synchronized(impl.observers.transformAndGet { _ + obs }(tx)).size
      logger.trace(s"$this observers: ${size}")
    }
    override def unobserve(obs: O => Unit): Unit = atomic { tx =>
    val size = tx.synchronized(impl.observers.transformAndGet { _ - obs }(tx)).size
      logger.trace(s"$this observers: ${size}")
    }
  }
  import scala.concurrent._

  private val pool = Executors.newCachedThreadPool()
  private implicit val myExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable) {
      pool.submit(runnable)
    }

    def reportFailure(t: Throwable) = {
      t.printStackTrace()
    }
  }

  def parallelForeach[A, B](elements: Iterable[A])(op: A => B) = {
    if (elements.isEmpty) {
      Nil
    } else {
      val iterator = elements.iterator
      val head = iterator.next()

      val futures = iterator.foldLeft(List[(A, Future[B])]()) { (futures, element) =>
        (element -> future {
          try {
            op(element)
          }
          catch {
            case rollback: Error with ControlThrowable => throw new InvocationTargetException(rollback)
          }
        }) :: futures
      }
      val headResult = Try { op(head) }
      val results = headResult :: futures.map {
        case (element, future) =>
          logger.trace(s"$this join $element")
          Await.ready(future, duration.Duration.Inf)
          future.value.get
      }

      logger.trace(s"$this fork/join completed")
      // TODO this should probably be converted into an exception thrown forward to
      // the original caller and be accumulated through all fork/joins along the path?
      results.foreach {
        case Failure(e: InvocationTargetException) => throw e.getTargetException()
        case Failure(e) => e.printStackTrace()
        case _ =>
      }
      results
    }
  }
}
