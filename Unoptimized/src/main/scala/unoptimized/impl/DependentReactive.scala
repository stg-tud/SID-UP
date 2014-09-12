package unoptimized
package impl

import java.util.UUID

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  private var _sourceDependencies = calculateSourceDependencies(null)
  override def sourceDependencies(transaction: Transaction) = _sourceDependencies

  protected def doReevaluation(transaction: Transaction, recalculateDependencies: Boolean, recalculateValueAndPulse: Boolean): Unit = {
    val pulse = if (recalculateValueAndPulse) {
      reevaluate(transaction: Transaction)
    } else {
      None
    }

    val sourceDependenciesChanged = if (recalculateDependencies) {
      val oldSourceDependencies = _sourceDependencies
      _sourceDependencies = calculateSourceDependencies(transaction)
      oldSourceDependencies != _sourceDependencies
    } else {
      false
    }

    doPulse(transaction, sourceDependenciesChanged, pulse)
  }

  protected def reevaluate(transaction: Transaction): Option[P]

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  protected val isDynamicNode: Boolean = false

  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }
  private var currentTransaction: Transaction = _
  private var reevaluationIssued: Boolean = _

  override def ping(transaction: Transaction): Unit = {
    val maybeReevaluationReady: DependentReactive.ReevaluationReadinessResult = synchronized {
      if (currentTransaction != transaction) {
        if (!hasPulsed(currentTransaction)) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, Previous transaction ${currentTransaction.uuid} not completed yet!")
        currentTransaction = transaction
        reevaluationIssued = false
      }

      if (reevaluationIssued) {
        DependentReactive.StaleNotification
      } else {
        if(isDynamicNode) updateDependencySubscriptions(transaction)
        
        // iterate all dependencies to
        //   a) see if one still must be updated this turn, and
        //   b) at the same time aggregate source id set and pulse changed bits, in case none is found
        var anyDependenciesChanged = false
        var anyPulse = false
        val waitingFor = lastDependencies.find { dependency =>
          if (dependency.isConnectedTo(transaction)) {
            if (dependency.hasPulsed(transaction)) {
              anyPulse = anyPulse | dependency.pulse(transaction).isDefined
              anyDependenciesChanged = anyDependenciesChanged | dependency.sourceDependenciesChanged(transaction)
              false
            } else {
              true
            }
          } else {
            false
          }
        }

        waitingFor match {
          case None =>
            reevaluationIssued = true
            DependentReactive.Ready(anyDependenciesChanged, anyPulse)
          case Some(waitingFor) =>
            DependentReactive.NotReady(waitingFor)
        }
      }
    }

    maybeReevaluationReady match {
      case DependentReactive.StaleNotification =>
      // ignore stale notification
      case DependentReactive.Ready(anyDependenciesChanged, anyPulse) =>
        doReevaluation(transaction, anyDependenciesChanged || (isDynamicNode  && anyPulse), anyPulse)
      case DependentReactive.NotReady(waitingFor) =>
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

object DependentReactive {
  sealed trait ReevaluationReadinessResult
  case object StaleNotification extends ReevaluationReadinessResult
  case class NotReady(foundDependency: Reactive[_, _]) extends ReevaluationReadinessResult
  case class Ready(anyDependenciesChanged: Boolean, anyPulse: Boolean) extends ReevaluationReadinessResult
}