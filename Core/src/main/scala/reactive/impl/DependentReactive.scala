package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.{InTxn, Ref}
import com.typesafe.scalalogging.slf4j.StrictLogging

trait DependentReactive[P] extends Reactive.Dependant with StrictLogging {
  self: ReactiveImpl[_, P] =>

  override def toString = name

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  protected[reactive] def sourceDependenciesPulsed(transaction: Transaction): Boolean =
    dependencies(transaction).forall { _.hasPulsed(transaction) }

  protected[reactive] def anySourcePulseChanged(transaction: Transaction): Boolean =
    dependencies(transaction).exists { _.pulse(transaction).hasChanged }

  protected[reactive] def anySourceDependenciesChanged(transaction: Transaction): Boolean =
    dependencies(transaction).exists { _.pulse(transaction).sourceDependencies.isDefined }

  protected[reactive] def ping(transaction: Transaction) = pingImpl(transaction)

  protected[reactive] def pingImpl(transaction: Transaction) = {
    logger.trace(s"$this got ping, dependencies pulsed: ${sourceDependenciesPulsed(transaction) }")
    if (sourceDependenciesPulsed(transaction)) {
      val pulse = if (anySourcePulseChanged(transaction)) reevaluate(transaction) else None
      val sourceDependencies = if (anySourceDependenciesChanged(transaction)) Some(calculateSourceDependencies(transaction)) else None
      setPulse(transaction, Pulse(pulse, sourceDependencies))
      transaction.pingDependants(dependants.snapshot)
    }
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

  /**
   * this method is called after all the source dependencies have pulsed.
   * it is expected to calculate the source dependencies of this reactive for the current transaction.
   * as any changes computed by this method might be rolled back by the transaction,
   * this should not have any side effects.
   *
   * @param transaction the transaction for which the pulse should be calculated
   * @return the pulse value of the current transaction or none if nothing happened
   */
  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(
      _.sourceDependencies(transaction)
    )
  }

  onCommit { transaction =>
    setSourceDependencies(sourceDependencies(transaction))
  }

  protected val cachedDependencies = Ref(calculateSourceDependencies(null))

  protected def setSourceDependencies(deps: Set[UUID]): Unit = cachedDependencies.single.set(deps)

  override protected[reactive] def sourceDependencies(transaction: Transaction): Set[UUID] =
    Option(transaction).flatMap { tx =>
      if (tx.hasPulsed(this)) tx.pulse(this).sourceDependencies else None
    }.getOrElse(cachedDependencies.single.get)

}
