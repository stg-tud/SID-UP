package reactive
package impl

import scala.collection.mutable
import com.typesafe.scalalogging.LazyLogging
import java.util.concurrent.Executors
import scala.util.Failure
import scala.util.Try
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.Executor

trait ReactiveImpl[O, P] extends Reactive[O, P] with LazyLogging {
  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction)).isEmpty

  private[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)

    val trace = Thread.currentThread().getStackTrace();
    var i = 0;
    while (!trace(i).toString().startsWith("reactive.")) i += 1
    while (trace(i).toString.startsWith("reactive.") && !trace(i).toString().startsWith("reactive.test.")) i += 1

    s"$unqualifiedClassname($hashCode) from ${trace(i)}"
  }
  override def toString = name

  private var currentTransaction: Transaction = _
  private var pulse: Option[P] = None
  // =============================================================================================
  // Trade-off: better speed for worse memory footprint:
  // Never drop pulse values.
  def pulse(transaction: Transaction): Option[P] = if (hasPulsed(transaction)) pulse else None
  def hasPulsed(transaction: Transaction): Boolean = currentTransaction == transaction
  // ---------------------------------------------------------------------------------------------
  // Trade-off: better memory footprint for worse speed:
  // Drop (outdated) pulse value when new transaction is observed
  //  def pulse(transaction: Transaction): Option[P] = {
  //    hasPulsed(transaction)
  //    pulse
  //  }
  //  def hasPulsed(transaction: Transaction): Boolean = {
  //    synchronized {
  //      if (currentTransaction == transaction) {
  //        true
  //      } else {
  //        pulse = None
  //        false
  //      }
  //    }
  //  }
  // =============================================================================================

  private var dependants = Set[Reactive.Dependant]()
  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant): Boolean = {
    synchronized {
      logger.trace(s"$dependant <~ $this [${Option(transaction).map { _.uuid }}]")
      dependants += dependant
      transaction != null && isPulseUpcoming(transaction)
    }
  }
  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant): Boolean = {
    synchronized {
      logger.trace(s"$dependant <!~ $this [${Option(transaction).map { _.uuid }}]")
      dependants -= dependant
      transaction != null && isPulseUpcoming(transaction)
    }
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) = {
    val pulsed = pulse.isDefined
    logger.trace(s"$this => Pulse($pulse, $sourceDependenciesChanged) [${Option(transaction).map { _.uuid }}]")
    val deptsToNotify = synchronized {
      this.pulse = pulse
      this.currentTransaction = transaction
      dependants
    }
    ReactiveImpl.parallelForeach(deptsToNotify) { _.ping(transaction, sourceDependenciesChanged, pulsed) }
    if (pulsed) {
      val value = getObserverValue(transaction, pulse.get)
      notifyObservers(transaction, value)
    }
  }
  protected def getObserverValue(transaction: Transaction, pulseValue: P): O

  // ====== Observing stuff ======

  private val observers = mutable.Set[O => Unit]()
  def observe(obs: O => Unit) = {
    observers += obs
    logger.trace(s"$this observers: ${observers.size}")
  }
  def unobserve(obs: O => Unit) = {
    observers -= obs
    logger.trace(s"$this observers: ${observers.size}")
  }

  private def notifyObservers(transaction: Transaction, value: O) = {
    logger.trace(s"$this -> Observers(${observers.size})")
    ReactiveImpl.parallelForeach(observers) { _(value) }
  }
}

object ReactiveImpl extends LazyLogging {
  import scala.concurrent._

  private implicit val myExecutionContext = ExecutionContext.fromExecutor(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue[Runnable]()))
//  private implicit val myExecutionContext = ExecutionContext.fromExecutor(new Executor{
//    override def execute(runnable: Runnable): Unit = {
//      runnable.run()
//    }
//  })

  def parallelForeach[A, B](elements: Iterable[A])(op: A => B) = {
    if (elements.isEmpty) {
      Nil
    } else {
      val iterator = elements.iterator
      val head = iterator.next()

      val futures = iterator.foldLeft(List[(A, Future[B])]()) { (futures, element) =>
        (element -> Future { op(element) }) :: futures
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
        case Failure(e) => e.printStackTrace()
        case _ =>
      }
      results
    }
  }
}
