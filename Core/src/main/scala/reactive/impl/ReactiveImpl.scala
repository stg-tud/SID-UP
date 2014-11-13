package reactive
package impl

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executors
import com.typesafe.scalalogging.LazyLogging
import reactive.Reactive._
import reactive.signals.Signal
import scala.concurrent.stm._
import scala.util.{ Failure, Try }
import scala.util.control.ControlThrowable
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.SynchronousQueue

trait ReactiveImpl[O, P] extends Reactive[O, P] with LazyLogging {
  override def isConnectedTo(transaction: Transaction) = (transaction.sources & sourceDependencies(transaction.stmTx)).nonEmpty

  private[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)

    val trace = Thread.currentThread().getStackTrace();
    var i = 0;
    while (!trace(i).toString().startsWith("reactive.")) i += 1
    while ((trace(i).toString.startsWith("reactive.") && !trace(i).toString().startsWith("reactive.test.")) || trace(i).toString().startsWith("scala.concurrent.stm.")) i += 1

    s"$unqualifiedClassname($hashCode) from ${trace(i)}"
  }

  override def toString = name

  private val pulse: TxnLocal[PulsedState[P]] = TxnLocal(Pending)

  override protected[reactive] def pulse(tx: InTxn): PulsedState[P] = tx.synchronized { pulse()(tx) }

  override protected[reactive] def hasPulsed(tx: InTxn): Boolean = tx.synchronized { pulse(tx).pulsed }

  private val dependants = Ref(Set[Reactive.Dependant]())

  override protected[reactive] def addDependant(tx: InTxn, dependant: Reactive.Dependant) = tx.synchronized {
    logger.trace(s"$dependant <~ $this [$tx]")
    dependants.transform(_ + dependant)(tx)
  }

  override protected[reactive] def removeDependant(tx: InTxn, dependant: Reactive.Dependant) = tx.synchronized {
    logger.trace(s"$dependant <!~ $this [$tx]")
    dependants.transform(_ - dependant)(tx)
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]): Unit = {
    val tx = transaction.stmTx

    // set pulse
    tx.synchronized {
      logger.trace(s"$this => Pulse($pulse, $sourceDependenciesChanged) [${Option(transaction)}")
      this.pulse.set(PulsedState(pulse))(tx)
    }

    val pulsed = pulse.isDefined

    ReactiveImpl.parallelForeach(tx.synchronized(dependants()(tx))) { dep =>
      dep.ping(transaction, sourceDependenciesChanged, pulsed)
    }.collect { case Failure(e) => throw e }

    if (pulsed) tx.synchronized {
      val obsToNotify = observers()(tx)
      if (obsToNotify.nonEmpty) {
        val value = getObserverValue(transaction, pulse.get)
        Txn.afterCommit { _ =>
          ReactiveImpl.parallelForeach(obsToNotify) {
            _(value)
          }.collect { case Failure(e) => e.printStackTrace() }
        }(tx)
      }
    }
  }

  protected def getObserverValue(transaction: Transaction, pulseValue: P): O

  // ====== Observing stuff ======

  private val observers = Ref(Set[O => Unit]())

  def observe(obs: O => Unit)(implicit inTxn: InTxn): Unit = {
    val size = inTxn.synchronized(observers.transformAndGet { _ + obs }(inTxn)).size
    logger.trace(s"$this observers: $size")
  }

  def unobserve(obs: O => Unit)(implicit inTxn: InTxn): Unit = {
    val size = inTxn.synchronized(observers.transformAndGet { _ - obs }(inTxn)).size
    logger.trace(s"$this observers: $size")
  }
}

object ReactiveImpl extends LazyLogging {

  trait ViewImpl[O] extends Reactive.View[O] {
    protected def impl: ReactiveImpl[O, _]

    override def log: Signal[Seq[O]] = atomic { impl.log(_) }

    override def observe(obs: O => Unit): Unit = atomic { tx =>
      val size = tx.synchronized(impl.observers.transformAndGet { _ + obs }(tx)).size
      logger.trace(s"$this observers: $size")
    }

    override def unobserve(obs: O => Unit): Unit = atomic { tx =>
      val size = tx.synchronized(impl.observers.transformAndGet { _ - obs }(tx)).size
      logger.trace(s"$this observers: $size")
    }
  }

  import scala.concurrent._

  private implicit val myExecutionContext = ExecutionContext.fromExecutor(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue[Runnable]()))
  
  def runWrappingRollbackErrors[A](op: => A) = {
    try {
      op
    } catch {
      case rollback: Error with ControlThrowable => throw new InvocationTargetException(rollback)
    }
  }

  def parallelForeach[A, B](elements: Iterable[A])(op: A => B) = {
    if (elements.isEmpty) {
      Nil
    } else {
      val iterator = elements.iterator
      val head = iterator.next()

      val futures = iterator.foldLeft(List[(A, Future[B])]()) { (futures, element) =>
        (element -> Future {
          runWrappingRollbackErrors { op(element) }
        }) :: futures
      }
      val headResult = Try { runWrappingRollbackErrors { op(head) } }
      val results = headResult :: futures.map {
        case (element, future) =>
          logger.trace(s"$this join $element")
          Await.ready(future, duration.Duration.Inf)
          future.value.get
      }

      logger.trace(s"$this fork/join completed")
      results.map {
        case Failure(e: InvocationTargetException) => Failure(e.getTargetException)
        case x => x
      }
    }
  }
}
