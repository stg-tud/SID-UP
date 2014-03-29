package reactive
package impl

import java.util.UUID

trait DependentReactive[P] extends Reactive.Dependant {
  self: ReactiveImpl[_, P] =>

  override def toString = name

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  protected[reactive] def sourceDependenciesPulsed(transaction: Transaction): Boolean =
    dependencies(transaction).forall { _.hasPulsed(transaction) }

  protected[reactive] def anySourcePulseChanged(transaction: Transaction): Boolean =
    dependencies(transaction).exists { _.pulse(transaction).hasChanged }

  protected[reactive] def anySourceDependenciesChanged(transaction: Transaction): Boolean =
    dependencies(transaction).exists { _.pulse(transaction).sourceDependencies.isDefined }

  override protected[reactive] def ping(transaction: Transaction) =
    if (sourceDependenciesPulsed(transaction)) {
      val pulse = if (anySourcePulseChanged(transaction)) reevaluate(transaction) else None
      val sourceDependencies = if (anySourceDependenciesChanged(transaction)) Some(calculateSourceDependencies(transaction)) else None
      setPulse(transaction, Pulse(pulse, sourceDependencies))
      transaction.pingDependants(dependants)
    }

  /**
   * this method is called after all the source dependencies have pulsed.
   * it is expected to calculate the pulse of this reactive for the current transaction.
   * as any changes computed by this method might be rolled back by the transaction,
   * this should not have any side effects.
   *
   * @param transaction the transaction for which the pulse should be calculated
   * @return the pulse value of the current transaction or none if nothing happened
   */
  protected def reevaluate(transaction: Transaction): Option[P]

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID]

  override protected[reactive] def sourceDependencies(transaction: Transaction): Set[UUID] = calculateSourceDependencies(transaction)

}
