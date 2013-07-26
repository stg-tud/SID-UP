package reactive
package impl

import java.util.UUID

trait DynamicDependentReactive {
  self: DependentReactive[_, _] =>

  protected def dependencies(transaction:Transaction): Set[Reactive[_, _, _]]
  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = _
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    if (currentTransaction == null || !currentTransaction.equals(transaction)) {
      anyDependenciesChanged = false;
      anyPulse = false;
    }

    val newDependencies = dependencies(transaction)
    lastDependencies.filterNot(newDependencies.contains(_)).foreach{
      anyPulse = true;
      anyDependenciesChanged = true;
      _.removeDependant(transaction, this)
    }
    newDependencies.filterNot(lastDependencies.contains(_)).foreach{
      anyPulse = true;
      anyDependenciesChanged = true;
      _.addDependant(transaction, this)
    }

    lastDependencies = newDependencies
    anyDependenciesChanged |= sourceDependenciesChanged
    anyPulse |= pulsed;

    if (!newDependencies.exists { dependency =>
      dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction)
    }) {
      doReevaluation(transaction, anyDependenciesChanged, anyPulse)
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction));
  }
}
