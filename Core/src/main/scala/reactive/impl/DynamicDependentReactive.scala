package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait DynamicDependentReactive extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }
  private var currentTransaction: Transaction = _
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _
  private var reevaluationIssued: Boolean = _

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val reevaluationReadinessResult = synchronized {
      if (currentTransaction != transaction) {
        if (!hasPulsed(currentTransaction)) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, Previous transaction ${currentTransaction.uuid} not completed yet!")
        currentTransaction = transaction
        anyDependenciesChanged = false
        anyPulse = false
        reevaluationIssued = false
      }

      if (reevaluationIssued) {
        DynamicDependentReactive.StaleNotification
      } else {
        anyDependenciesChanged |= sourceDependenciesChanged
        anyPulse |= pulsed

        if(pulsed) {
        	anyDependenciesChanged |= updateDependencySubscriptions(transaction)
        }

        val waitingFor = lastDependencies.find(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction))

        waitingFor match {
          case None =>
            reevaluationIssued = true
            DynamicDependentReactive.Ready(anyDependenciesChanged, anyPulse)
          case Some(waitingFor) =>
            DynamicDependentReactive.NotReady(waitingFor)
        }
      }
    }
    
    reevaluationReadinessResult match {
      case DynamicDependentReactive.StaleNotification =>
      // ignore stale notification
      case DynamicDependentReactive.Ready(anyDependenciesChanged, anyPulse) =>
        doReevaluation(transaction, anyDependenciesChanged, anyPulse)
      case DynamicDependentReactive.NotReady(waitingFor) =>
        logger.trace(s"$name still waits for updates from $waitingFor and possibly more)")
    }
  }

  private def updateDependencySubscriptions(transaction: Transaction): Boolean = {
    val newDependencies = dependencies(transaction)
    val unsubscribe = lastDependencies.diff(newDependencies)
    val subscribe = newDependencies.diff(lastDependencies)
    lastDependencies = newDependencies

    unsubscribe.foreach { dep =>
      dep.removeDependant(transaction, this)
    }
    subscribe.foreach { dep =>
      dep.addDependant(transaction, this)
    }

    unsubscribe.nonEmpty | subscribe.nonEmpty
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }
}

object DynamicDependentReactive {
  sealed trait ReevaluationReadinessResult
  case object StaleNotification extends ReevaluationReadinessResult
  case class NotReady(waitingFor: Reactive[_, _]) extends ReevaluationReadinessResult
  case class Ready(anyDependenciesChanged: Boolean, anyPulse: Boolean) extends ReevaluationReadinessResult
}
