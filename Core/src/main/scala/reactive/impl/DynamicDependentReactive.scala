package reactive.impl

import reactive._
import java.util.UUID

trait DynamicDependentReactive {
  self: Reactive[_, _] with DependentReactive[_] =>

  dependencies(new Transaction(Set())).foreach(_.addDependant(new Transaction(Set()), this))

  override protected[reactive] def anySourceDependenciesChanged(transaction: Transaction): Boolean = calculateSourceDependencies(transaction) != cachedDependencies.single.get

  override protected[reactive] def ping(transaction: Transaction) = {
    dependencies(transaction).foreach(_.addDependant(transaction, this))
    pingImpl(transaction)
  }

  override protected[reactive] def sourceDependencies(transaction: Transaction): Set[UUID] = calculateSourceDependencies(transaction)
}
