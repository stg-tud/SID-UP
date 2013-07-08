package reactive
package impl

import java.util.UUID

trait MultiDependentReactive[P] extends DependentReactive[P] {
  self: ReactiveImpl[_, _, P] =>

  protected val dependencies: Set[Reactive[_, _, _]]
  dependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = _
  private var pendingNotifications: Int = 0
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    if (currentTransaction == null || !currentTransaction.equals(transaction)) {
      if (pendingNotifications != 0) throw new IllegalStateException("Previous transaction not completed yet!")
      pendingNotifications = dependencies.count(_.isConnectedTo(transaction)) - 1
      anyDependenciesChanged = sourceDependenciesChanged
      anyPulse = pulsed;
    } else {
      pendingNotifications -= 1;
      anyDependenciesChanged |= sourceDependenciesChanged
      anyPulse |= pulsed;
      if (pendingNotifications == 0) {
        doReevaluation(transaction, anyDependenciesChanged, anyPulse)
      }
    }
  }

  protected def reevaluateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(transaction));
  }
}