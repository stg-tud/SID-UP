package reactive
package impl

import scala.collection.mutable
import com.typesafe.scalalogging.slf4j.Logging
import java.util.concurrent.Executors
import scala.util.Failure
import scala.util.Try
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic
import Reactive._
import scala.concurrent.stm.Txn

trait ReactiveImpl[O, P] extends Reactive[O, P] with Logging {
  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction)).isEmpty

  private[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)
    s"$unqualifiedClassname($hashCode)"
  }

  override def toString = name

  private val pulse: Ref[PulsedState[P]] = Ref(Pending)
  def pulse(transaction: Transaction): PulsedState[P] = atomic { tx => pulse()(tx) }
  def hasPulsed(transaction: Transaction): Boolean = atomic { tx => pulse()(tx).pulsed }

  private val dependants = Ref(Set[Reactive.Dependant]())

  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    synchronized {
      logger.trace(s"$dependant <~ $this [${Option(transaction).map { _.uuid } }]")
      atomic{ tx =>
        dependants.transform(_ + dependant)(tx)
      }
    }
  }

  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    synchronized {
      logger.trace(s"$dependant <!~ $this [${Option(transaction).map { _.uuid } }]")
      atomic{ tx =>
        dependants.transform(_ - dependant)(tx)
      }
    }
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) {
    synchronized {
      atomic { tx =>
        logger.trace(s"$this => Pulse($pulse, $sourceDependenciesChanged) [${Option(transaction).map { _.uuid } }]")
        this.pulse.set(PulsedState(pulse))(tx)
        Txn.beforeCommit(inTxnBeforeCommit => {
          this.pulse.set(Pending)(inTxnBeforeCommit)
        })(tx)
        val pulsed = pulse.isDefined
        dependants()(tx).foreach { _.apply(transaction, sourceDependenciesChanged, pulsed) }
        if (pulsed) {
          val value = getObserverValue(transaction, pulse.get)
          Txn.afterCommit{_ =>
            notifyObservers(transaction, value)
          }(tx)
        }
      }
    }
  }

  protected def getObserverValue(transaction: Transaction, pulseValue: P): O

  // ====== Observing stuff ======

  private val observers = mutable.Set[O => Unit]()

  def observe(obs: O => Unit) {
    observers += obs
    logger.trace(s"$this observers: ${observers.size }")
  }

  def unobserve(obs: O => Unit) {
    observers -= obs
    logger.trace(s"$this observers: ${observers.size }")
  }

  private def notifyObservers(transaction: Transaction, value: O) {
    logger.trace(s"$this -> Observers(${observers.size })")
    ReactiveImpl.parallelForeach(observers) { _(value) }
  }
}

object ReactiveImpl extends Logging {

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
    }
    else {
      val iterator = elements.iterator
      val head = iterator.next()

      val futures = iterator.foldLeft(List[(A, Future[B])]()) { (futures, element) =>
        (element -> future { op(element) }) :: futures
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
