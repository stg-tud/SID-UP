package unoptimized
package impl

import java.util.UUID

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  override def toString = name

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

  override def ping(transaction: Transaction): Unit = {
    if (!hasPulsed(transaction)) {
      val (waitingFor, anyDependenciesChanged, anyPulse) = synchronized {
        if (currentTransaction != transaction) {
          if (!hasPulsed(currentTransaction)) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, Previous transaction ${currentTransaction.uuid} not completed yet!")
          currentTransaction = transaction
        }

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
        (waitingFor, anyDependenciesChanged, anyPulse)
      }

      if (waitingFor.isEmpty) {
        doReevaluation(transaction, anyDependenciesChanged | (isDynamicNode & anyPulse), anyPulse)
      } else {
        logger.trace(s"$name still waits for updates from $waitingFor and possibly more)")
      }
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }

}
