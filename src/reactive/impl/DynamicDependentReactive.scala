package reactive
package impl

import java.util.UUID

trait DynamicDependentReactive[P] extends DependentReactive[P] {
  self: ReactiveImpl[_, _, P] =>

  protected def dependencies(transaction:Transaction): Set[Reactive[_, _, _]]
  private var currentDependencies = dependencies(null)
  currentDependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = _
  private var notificationsReceived: Int = 0
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    if (currentTransaction == null || !currentTransaction.equals(transaction)) {
      notificationsReceived = 0;
      anyDependenciesChanged = false;
      anyPulse = false;
    }

    val oldDependencies = currentDependencies
    currentDependencies = dependencies(transaction)
    oldDependencies.filterNot(currentDependencies.contains(_)).foreach{
      anyPulse = true;
      anyDependenciesChanged = true;
      _.removeDependant(transaction, this)
    }
    currentDependencies.filterNot(oldDependencies.contains(_)).foreach{
      anyPulse = true;
      anyDependenciesChanged = true;
      _.addDependant(transaction, this)
    }
    
    
    notificationsReceived += 1;
    anyDependenciesChanged |= sourceDependenciesChanged
    anyPulse |= pulsed;

    if (currentDependencies.find { dependency =>
      dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction)
    }.isEmpty) {
      doReevaluation(transaction, anyDependenciesChanged, anyPulse)
    }
  }

  protected def reevaluateSourceDependencies(transaction: Transaction): Set[UUID] = {
    currentDependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(transaction));
  }
}